import express from "express";
import mongoose from "mongoose";
import cors from "cors";
import bcrypt from "bcrypt";
import jwt from "jsonwebtoken";
import dotenv from "dotenv";

dotenv.config();

const app = express();
app.use(cors());
app.use(express.json());

// Ruta rápida para probar si el servidor está vivo
app.get("/", (req, res) => res.send("API OK"));

// =====================
// Modelos
// =====================
const UserSchema = new mongoose.Schema({
  name: String,
  email: { type: String, unique: true },
  passwordHash: String,
});
const User = mongoose.model("User", UserSchema);

const ExerciseSchema = new mongoose.Schema({
  name: String,
  muscleGroup: String,
});
const Exercise = mongoose.model("Exercise", ExerciseSchema);

// =====================
// Middleware JWT
// =====================
function auth(req, res, next) {
  const h = req.headers.authorization || "";
  const token = h.startsWith("Bearer ") ? h.slice(7) : "";
  if (!token) return res.status(401).json({ message: "No token" });

  try {
    req.user = jwt.verify(token, process.env.JWT_SECRET);
    next();
  } catch {
    return res.status(401).json({ message: "Invalid token" });
  }
}

// =====================
// Rutas Auth
// =====================
app.post("/auth/register", async (req, res) => {
  try {
    const { name, email, password } = req.body;

    if (!name || !email || !password) {
      return res.status(400).json({ message: "Faltan datos" });
    }

    const passwordHash = await bcrypt.hash(password, 10);
    const u = await User.create({ name, email, passwordHash });

    const token = jwt.sign({ userId: u._id }, process.env.JWT_SECRET, {
      expiresIn: "7d",
    });

    res.json({ token, name: u.name, email: u.email });
  } catch (error) {
    // si el email ya existe, Mongo lanza error de duplicado
    return res.status(400).json({ message: "Error al registrar (email quizá ya existe)" });
  }
});

app.post("/auth/login", async (req, res) => {
  try {
    const { email, password } = req.body;

    if (!email || !password) {
      return res.status(400).json({ message: "Faltan datos" });
    }

    const u = await User.findOne({ email });
    if (!u) return res.status(401).json({ message: "Credenciales inválidas" });

    const ok = await bcrypt.compare(password, u.passwordHash);
    if (!ok) return res.status(401).json({ message: "Credenciales inválidas" });

    const token = jwt.sign({ userId: u._id }, process.env.JWT_SECRET, {
      expiresIn: "7d",
    });

    res.json({ token, name: u.name, email: u.email });
  } catch (error) {
    return res.status(500).json({ message: "Error en el servidor" });
  }
});

// =====================
// Rutas protegidas
// =====================
app.get("/exercises", auth, async (req, res) => {
  try {
    const items = await Exercise.find().limit(200);
    res.json(items);
  } catch (error) {
    res.status(500).json({ message: "Error al obtener ejercicios" });
  }
});

app.post("/progress", auth, async (req, res) => {
  try {
    // Aquí luego guardarás progress en MongoDB
    res.status(200).json({ message: "Progreso sincronizado" });
  } catch (error) {
    res.status(500).json({ message: "Error al sincronizar" });
  }
});

// =====================
// Conexión Mongo + Arranque servidor
// =====================
mongoose
  .connect(process.env.MONGO_URI)
  .then(() => {
    console.log("✅ Conectado a MongoDB Atlas");

    const PORT = process.env.PORT || 3000;
    app.listen(PORT, "0.0.0.0", () => {
      console.log(`🚀 Servidor corriendo en puerto ${PORT}`);
    });
  })
  .catch((err) => {
    console.error("❌ Error de conexión a MongoDB:", err.message);

    if (err.message.includes("ECONNREFUSED") || err.message.includes("querySrv")) {
      console.log("👉 TIP: Cambia DNS a 8.8.8.8 / 1.1.1.1 o prueba hotspot del celular.");
    }

    process.exit(1);
  });
