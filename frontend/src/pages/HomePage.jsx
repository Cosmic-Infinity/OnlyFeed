import { useState } from "react";
import { Link } from "react-router-dom";
import FeedList from "../components/FeedList";
import PostComposer from "../components/PostComposer";
import { useAuth } from "../context/AuthContext";

export default function HomePage() {
  const { isAuthenticated } = useAuth();
  const [tab, setTab] = useState("global");
  const [reloadToken, setReloadToken] = useState(0);

  const endpoint = tab === "global" ? "/api/posts/global?page=0&size=20" : "/api/posts/following?page=0&size=20";

  return (
    <section className="stack-lg">
      <div className="tabs card" style={{ justifyContent: "center", flexDirection: "column", alignItems: "center", gap: "0.75rem" }}>
        <div style={{ display: "flex", gap: "0.5rem" }}>
          <button className={tab === "global" ? "tab active" : "tab"} onClick={() => setTab("global")}>
            Global Feed
          </button>
          <button className={tab === "following" ? "tab active" : "tab"} onClick={() => setTab("following")}>
            Following Feed
          </button>
        </div>
        {!isAuthenticated && (
          <p className="muted" style={{ fontSize: "0.85rem", margin: 0 }}>
            <Link to="/login">Log in</Link> to post, like, comment, or follow users.
          </p>
        )}
      </div>

      {isAuthenticated && <PostComposer onPosted={() => setReloadToken((value) => value + 1)} />}

      <FeedList endpoint={endpoint} emptyText={tab === "global" ? "No posts yet." : "No posts from followed users yet."} reloadToken={reloadToken} />
    </section>
  );
}
