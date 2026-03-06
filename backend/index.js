import express from "express";
import cors from "cors";
import bcrypt from "bcryptjs";
import jwt from "jsonwebtoken";
import dotenv from "dotenv";
import { supabase } from "./supabaseClient.js";

dotenv.config();

const {
  PORT = 3000,
  JWT_SECRET,
  CORS_ORIGIN = "*",
  RAPIDAPI_KEY,
  RAPIDAPI_HOST = "exercisedb.p.rapidapi.com",
  EXERCISEDB_BASE_URL = "https://exercisedb.p.rapidapi.com",
  EXERCISE_CACHE_TTL_MINUTES = "720",
} = process.env;

if (!JWT_SECRET) throw new Error("Falta JWT_SECRET en variables de entorno");

const app = express();
app.use(
  cors({
    origin: CORS_ORIGIN === "*" ? true : CORS_ORIGIN,
  })
);
app.use(express.json());

app.get("/", (_req, res) => {
  res.json({ status: "ok", service: "gymtrack-api", storage: "supabase" });
});

let exerciseCache = { updatedAt: 0, items: [] };

function auth(req, res, next) {
  const h = req.headers.authorization || "";
  const token = h.startsWith("Bearer ") ? h.slice(7) : "";
  if (!token) return res.status(401).json({ message: "No token" });

  try {
    req.user = jwt.verify(token, JWT_SECRET);
    return next();
  } catch {
    return res.status(401).json({ message: "Invalid token" });
  }
}

function mapExerciseDto(item) {
  const id = item.id || item._id;
  if (!id || !item.name) return null;

  return {
    id,
    name: item.name,
    bodyPart: item.bodyPart || null,
    equipment: item.equipment || null,
    target: item.target || null,
    muscleGroup: item.muscleGroup || item.target || null,
  };
}

async function fetchExercisesFromRapidApi(limit = 200, offset = 0) {
  if (!RAPIDAPI_KEY) return [];

  const url = `${EXERCISEDB_BASE_URL}/exercises?limit=${limit}&offset=${offset}`;
  const response = await fetch(url, {
    headers: {
      "x-rapidapi-key": RAPIDAPI_KEY,
      "x-rapidapi-host": RAPIDAPI_HOST,
    },
  });

  if (!response.ok) {
    const body = await response.text();
    throw new Error(`ExerciseDB error ${response.status}: ${body}`);
  }

  const data = await response.json();
  if (!Array.isArray(data)) return [];
  return data.map(mapExerciseDto).filter(Boolean);
}

async function getExercisesOfflineFirst() {
  const ttlMs = Number(EXERCISE_CACHE_TTL_MINUTES) * 60 * 1000;
  const cacheFresh = exerciseCache.items.length > 0 && Date.now() - exerciseCache.updatedAt < ttlMs;
  if (cacheFresh) return exerciseCache.items;

  try {
    const remoteItems = await fetchExercisesFromRapidApi(200, 0);
    if (remoteItems.length > 0) {
      exerciseCache = { updatedAt: Date.now(), items: remoteItems };
    }
    return exerciseCache.items;
  } catch {
    return exerciseCache.items;
  }
}

app.post("/auth/register", async (req, res) => {
  try {
    const { name, email, password } = req.body;
    const normalizedEmail = String(email || "").toLowerCase().trim();

    if (!name || !normalizedEmail || !password) {
      return res.status(400).json({ message: "Faltan datos" });
    }

    const { data: exists, error: existsError } = await supabase
      .from("users")
      .select("id")
      .eq("email", normalizedEmail)
      .maybeSingle();

    if (existsError) return res.status(500).json({ message: "Error al validar usuario" });
    if (exists) return res.status(409).json({ message: "Email ya registrado" });

    const passwordHash = await bcrypt.hash(password, 10);
    const { data: created, error: insertError } = await supabase
      .from("users")
      .insert({
        name: String(name).trim(),
        email: normalizedEmail,
        password_hash: passwordHash,
      })
      .select("id,name,email")
      .single();

    if (insertError || !created) return res.status(500).json({ message: "Error al registrar" });

    const token = jwt.sign({ userId: created.id }, JWT_SECRET, { expiresIn: "7d" });
    return res.status(201).json({ token, name: created.name, email: created.email });
  } catch {
    return res.status(500).json({ message: "Error al registrar" });
  }
});

app.post("/auth/login", async (req, res) => {
  try {
    const { email, password } = req.body;
    const normalizedEmail = String(email || "").toLowerCase().trim();

    if (!normalizedEmail || !password) {
      return res.status(400).json({ message: "Faltan datos" });
    }

    const { data: user, error } = await supabase
      .from("users")
      .select("id,name,email,password_hash")
      .eq("email", normalizedEmail)
      .maybeSingle();

    if (error) return res.status(500).json({ message: "Error en login" });
    if (!user) return res.status(401).json({ message: "Credenciales invalidas" });

    const ok = await bcrypt.compare(password, user.password_hash);
    if (!ok) return res.status(401).json({ message: "Credenciales invalidas" });

    const token = jwt.sign({ userId: user.id }, JWT_SECRET, { expiresIn: "7d" });
    return res.json({ token, name: user.name, email: user.email });
  } catch {
    return res.status(500).json({ message: "Error en login" });
  }
});

app.get("/exercises", auth, async (_req, res) => {
  try {
    const items = await getExercisesOfflineFirst();
    return res.json(items);
  } catch (error) {
    return res.status(500).json({ message: "Error al obtener ejercicios", detail: error.message });
  }
});

app.get("/routines", auth, async (req, res) => {
  try {
    const { data, error } = await supabase
      .from("routines")
      .select("id,name,exercise_ids,updated_at")
      .eq("user_id", req.user.userId)
      .order("updated_at", { ascending: false });

    if (error) return res.status(500).json({ message: "Error al obtener rutinas" });

    return res.json(
      (data || []).map((r) => ({
        _id: r.id,
        name: r.name,
        exerciseIds: r.exercise_ids || [],
      }))
    );
  } catch {
    return res.status(500).json({ message: "Error al obtener rutinas" });
  }
});

app.post("/routines", auth, async (req, res) => {
  try {
    const { name } = req.body;
    if (!name || !String(name).trim()) return res.status(400).json({ message: "Nombre requerido" });

    const { data: routine, error } = await supabase
      .from("routines")
      .insert({
        user_id: req.user.userId,
        name: String(name).trim(),
        exercise_ids: [],
        updated_at: new Date().toISOString(),
      })
      .select("id,name,exercise_ids")
      .single();

    if (error || !routine) return res.status(500).json({ message: "Error al crear rutina" });
    return res.status(201).json({ _id: routine.id, name: routine.name, exerciseIds: routine.exercise_ids || [] });
  } catch {
    return res.status(500).json({ message: "Error al crear rutina" });
  }
});

app.put("/routines/:id", auth, async (req, res) => {
  try {
    const { id } = req.params;
    const { name } = req.body;
    if (!name || !String(name).trim()) return res.status(400).json({ message: "Nombre requerido" });

    const { data: routine, error } = await supabase
      .from("routines")
      .update({ name: String(name).trim(), updated_at: new Date().toISOString() })
      .eq("id", id)
      .eq("user_id", req.user.userId)
      .select("id,name,exercise_ids")
      .maybeSingle();

    if (error) return res.status(500).json({ message: "Error al actualizar rutina" });
    if (!routine) return res.status(404).json({ message: "Rutina no encontrada" });

    return res.json({ _id: routine.id, name: routine.name, exerciseIds: routine.exercise_ids || [] });
  } catch {
    return res.status(500).json({ message: "Error al actualizar rutina" });
  }
});

app.delete("/routines/:id", auth, async (req, res) => {
  try {
    const { id } = req.params;

    const { data: existing, error: findError } = await supabase
      .from("routines")
      .select("id")
      .eq("id", id)
      .eq("user_id", req.user.userId)
      .maybeSingle();

    if (findError) return res.status(500).json({ message: "Error al eliminar rutina" });
    if (!existing) return res.status(404).json({ message: "Rutina no encontrada" });

    const { error: deleteError } = await supabase.from("routines").delete().eq("id", id).eq("user_id", req.user.userId);
    if (deleteError) return res.status(500).json({ message: "Error al eliminar rutina" });

    return res.status(204).send();
  } catch {
    return res.status(500).json({ message: "Error al eliminar rutina" });
  }
});

app.put("/routines/:id/exercises", auth, async (req, res) => {
  try {
    const { id } = req.params;
    const { exerciseIds } = req.body;

    if (!Array.isArray(exerciseIds)) {
      return res.status(400).json({ message: "exerciseIds debe ser arreglo" });
    }

    const normalized = [...new Set(exerciseIds.map((e) => String(e).trim()).filter(Boolean))];

    const { data: routine, error } = await supabase
      .from("routines")
      .update({ exercise_ids: normalized, updated_at: new Date().toISOString() })
      .eq("id", id)
      .eq("user_id", req.user.userId)
      .select("id,name,exercise_ids")
      .maybeSingle();

    if (error) return res.status(500).json({ message: "Error al sincronizar ejercicios de rutina" });
    if (!routine) return res.status(404).json({ message: "Rutina no encontrada" });

    return res.json({ _id: routine.id, name: routine.name, exerciseIds: routine.exercise_ids || [] });
  } catch {
    return res.status(500).json({ message: "Error al sincronizar ejercicios de rutina" });
  }
});

app.post("/progress", auth, async (req, res) => {
  try {
    const { dateIso, weight, note } = req.body;
    if (!dateIso || typeof weight !== "number") {
      return res.status(400).json({ message: "dateIso y weight son requeridos" });
    }

    const { error } = await supabase.from("progress").insert({
      user_id: req.user.userId,
      date_iso: dateIso,
      weight,
      note: note ?? null,
    });

    if (error) return res.status(500).json({ message: "Error al sincronizar progreso" });

    return res.status(200).json({ message: "Progreso sincronizado" });
  } catch {
    return res.status(500).json({ message: "Error al sincronizar progreso" });
  }
});

app.listen(PORT, "0.0.0.0", () => {
  console.log(`gymtrack-api running on port ${PORT} (storage=supabase)`);
});
