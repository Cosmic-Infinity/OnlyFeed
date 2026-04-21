import { useCallback, useEffect, useState } from "react";
import { apiRequest } from "../api/http";
import { useAuth } from "../context/AuthContext";
import PostCard from "./PostCard";

export default function FeedList({ endpoint, emptyText, reloadToken }) {
  const { token } = useAuth();
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const page = await apiRequest(endpoint, { token });
      setPosts(page.content || []);
    } catch (err) {
      setError(err.message);
      setPosts([]);
    } finally {
      setLoading(false);
    }
  }, [endpoint, token]);

  useEffect(() => {
    load();
  }, [load, reloadToken]);

  if (loading) {
    return <p className="muted">Loading feed...</p>;
  }

  if (error) {
    return <p className="inline-error">{error}</p>;
  }

  if (posts.length === 0) {
    return <p className="muted" style={{ textAlign: "center", padding: "2rem 0" }}>{emptyText}</p>;
  }

  return (
    <div className="feed-list">
      {posts.map((post) => (
        <PostCard key={post.id} post={post} onChanged={load} />
      ))}
    </div>
  );
}
