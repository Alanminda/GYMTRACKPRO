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

const EXTERNAL_PAGE_SIZE = Number(process.env.EXERCISE_FETCH_PAGE_SIZE || "50");
const EXTERNAL_FULL_FETCH_LIMIT = Number(process.env.EXERCISE_FULL_FETCH_LIMIT || "2000");
const MAX_CACHE_ITEMS = Number(process.env.EXERCISE_CACHE_MAX_ITEMS || "20000");
const MAX_EXPANSION_STEPS_PER_REQUEST = 80;
let exerciseCache = { updatedAt: 0, items: [], remoteOffset: 0, sourceExhausted: false };

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
    gifUrl: item.gifUrl || item.gif_url || null,
    bodyPart: item.bodyPart || null,
    equipment: item.equipment || null,
    target: item.target || null,
    muscleGroup: item.muscleGroup || item.target || null,
    secondaryMuscles: Array.isArray(item.secondaryMuscles) ? item.secondaryMuscles : [],
    instructions: Array.isArray(item.instructions) ? item.instructions : [],
  };
}

async function fetchExercisesFromRapidApi(limit = EXTERNAL_PAGE_SIZE, offset = 0) {
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

function isCacheFresh() {
  const ttlMs = Number(EXERCISE_CACHE_TTL_MINUTES) * 60 * 1000;
  return exerciseCache.items.length > 0 && Date.now() - exerciseCache.updatedAt < ttlMs;
}

async function refillCacheIfExpired() {
  if (isCacheFresh()) return;
  try {
    const firstPage = await fetchExercisesFromRapidApi(EXTERNAL_PAGE_SIZE, 0);
    exerciseCache = {
      updatedAt: Date.now(),
      items: firstPage,
      remoteOffset: firstPage.length,
      sourceExhausted: firstPage.length === 0 || firstPage.length >= MAX_CACHE_ITEMS,
    };
  } catch {
    // Si falla remoto, se conserva cache anterior.
  }
}

async function ensureCacheSize(minSize) {
  await refillCacheIfExpired();

  let steps = 0;
  while (exerciseCache.items.length < minSize && !exerciseCache.sourceExhausted && steps < MAX_EXPANSION_STEPS_PER_REQUEST) {
    const canContinue = await expandCacheByOnePage();
    if (!canContinue) break;
    steps += 1;
  }
}

async function expandCacheByOnePage() {
  if (exerciseCache.items.length >= MAX_CACHE_ITEMS || exerciseCache.sourceExhausted) {
    exerciseCache = { ...exerciseCache, sourceExhausted: true };
    return false;
  }

  const page = await fetchExercisesFromRapidApi(EXTERNAL_PAGE_SIZE, exerciseCache.remoteOffset);
  if (!page.length) {
    exerciseCache = { ...exerciseCache, updatedAt: Date.now(), sourceExhausted: true };
    return false;
  }

  const beforeLength = exerciseCache.items.length;
  let merged = mergeUniqueExercises(exerciseCache.items, page);

  const remoteOffset = exerciseCache.remoteOffset + page.length;
  const noGrowth = merged.length === beforeLength;
  if (noGrowth && exerciseCache.remoteOffset > 0) {
    // Fallback: algunos proveedores ignoran offset. Intentamos carga grande unica.
    const fullPage = await fetchExercisesFromRapidApi(EXTERNAL_FULL_FETCH_LIMIT, 0);
    merged = mergeUniqueExercises(merged, fullPage);
  }

  const stillNoGrowth = merged.length === beforeLength;
  const reachedCacheMax = merged.length >= MAX_CACHE_ITEMS;
  exerciseCache = {
    updatedAt: Date.now(),
    items: reachedCacheMax ? merged.slice(0, MAX_CACHE_ITEMS) : merged,
    remoteOffset,
    sourceExhausted: stillNoGrowth || reachedCacheMax,
  };

  return !exerciseCache.sourceExhausted;
}

function mergeUniqueExercises(base, page) {
  const existing = new Set(base.map((e) => e.id));
  const merged = [...base];
  for (const item of page) {
    if (!existing.has(item.id)) {
      merged.push(item);
      existing.add(item.id);
    }
  }
  return merged;
}

function filterExercises(list, q) {
  const query = (q || "").trim().toLowerCase();
  if (!query) return list;
  return list.filter((e) => {
    return (
      e.name.toLowerCase().includes(query) ||
      (e.muscleGroup || "").toLowerCase().includes(query) ||
      (e.bodyPart || "").toLowerCase().includes(query) ||
      (e.target || "").toLowerCase().includes(query) ||
      (e.equipment || "").toLowerCase().includes(query)
    );
  });
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

app.get("/exercises", auth, async (req, res) => {
  try {
    const limit = Math.min(Math.max(Number(req.query.limit) || 50, 1), 200);
    const offset = Math.max(Number(req.query.offset) || 0, 0);
    const q = String(req.query.q || "");

    if (!q.trim()) {
      await ensureCacheSize(Math.min(offset + limit, MAX_CACHE_ITEMS));
      return res.json(exerciseCache.items.slice(offset, offset + limit));
    }

    await refillCacheIfExpired();

    let filtered = filterExercises(exerciseCache.items, q);
    let steps = 0;
    while (filtered.length < offset + limit && !exerciseCache.sourceExhausted && steps < MAX_EXPANSION_STEPS_PER_REQUEST) {
      await expandCacheByOnePage();
      filtered = filterExercises(exerciseCache.items, q);
      steps += 1;
    }

    return res.json(filtered.slice(offset, offset + limit));
  } catch (error) {
    return res.status(500).json({ message: "Error al obtener ejercicios", detail: error.message });
  }
});

app.get("/exercises/debug", auth, (_req, res) => {
  return res.json({
    cacheItems: exerciseCache.items.length,
    remoteOffset: exerciseCache.remoteOffset,
    sourceExhausted: exerciseCache.sourceExhausted,
    externalPageSize: EXTERNAL_PAGE_SIZE,
    maxCacheItems: MAX_CACHE_ITEMS,
  });
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
