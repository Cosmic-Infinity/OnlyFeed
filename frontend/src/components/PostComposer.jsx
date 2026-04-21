import { useState } from "react";
import { apiRequest } from "../api/http";
import { useAuth } from "../context/AuthContext";

const MAX_LEN = 280;

export default function PostComposer({ onPosted }) {
  const { token, isAuthenticated } = useAuth();
  const [content, setContent] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  const remaining = MAX_LEN - content.length;

  const submit = async (event) => {
    event.preventDefault();
    if (!isAuthenticated) {
      setError("Please log in to create a post.");
      return;
    }

    const trimmed = content.trim();
    if (!trimmed) {
      setError("Post cannot be empty.");
      return;
    }

    setSubmitting(true);
    setError("");
    try {
      const newPost = await apiRequest("/api/posts", {
        method: "POST",
        token,
        body: { content: trimmed },
      });
      setContent("");
      onPosted?.(newPost);
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form className="card composer" onSubmit={submit}>
      <textarea value={content} onChange={(event) => setContent(event.target.value)} maxLength={MAX_LEN} placeholder="Share what's on your mind..." />
      <div className="composer-row">
        <span className={remaining < 20 ? "counter warning" : "counter"}>{remaining} chars left</span>
        <button className="btn primary" type="submit" disabled={submitting}>
          {submitting ? "Posting..." : "Post"}
        </button>
      </div>
      {error && <p className="inline-error">{error}</p>}
    </form>
  );
}
