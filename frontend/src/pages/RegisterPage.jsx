import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { apiRequest } from "../api/http";
import { useAuth } from "../context/AuthContext";

export default function RegisterPage() {
  const { setAuth } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({ username: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const submit = async (event) => {
    event.preventDefault();
    setError("");
    setLoading(true);
    try {
      const result = await apiRequest("/api/auth/register", {
        method: "POST",
        body: form,
      });
      setAuth(result);
      navigate("/");
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="auth-page card">
      <h1>Create account</h1>
      <form onSubmit={submit} className="stack">
        <label>
          Username (3-30 chars)
          <input minLength={3} maxLength={30} value={form.username} onChange={(event) => setForm({ ...form, username: event.target.value })} required />
        </label>
        <label>
          Password (minimum 6 chars)
          <input type="password" minLength={6} value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} required />
        </label>
        <button className="btn primary" disabled={loading} type="submit">
          {loading ? "Creating..." : "Create account"}
        </button>
      </form>
      {error && <p className="inline-error">{error}</p>}
      <p>
        Already have an account? <Link to="/login">Login</Link>
      </p>
    </section>
  );
}
