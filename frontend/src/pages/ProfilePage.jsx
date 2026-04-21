import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { apiRequest } from "../api/http";
import FeedList from "../components/FeedList";
import { useAuth } from "../context/AuthContext";

export default function ProfilePage() {
  const { username } = useParams();
  const { isAuthenticated, token, userId } = useAuth();

  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [tab, setTab] = useState("posts");
  const [reloadToken, setReloadToken] = useState(0);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setError("");
      try {
        const data = await apiRequest(`/api/users/profile/${username}`, { token });
        setProfile(data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [username, token]);

  const toggleFollow = async () => {
    if (!profile || !isAuthenticated) {
      return;
    }

    const method = profile.followedByMe ? "DELETE" : "POST";
    await apiRequest(`/api/users/${profile.id}/follow`, { method, token });

    setProfile((current) => {
      if (!current) {
        return current;
      }

      const nowFollowed = !current.followedByMe;
      const followerCount = nowFollowed ? current.followerCount + 1 : current.followerCount - 1;
      return { ...current, followedByMe: nowFollowed, followerCount };
    });
  };

  if (loading) {
    return <p className="muted">Loading profile...</p>;
  }

  if (error) {
    return <p className="inline-error">{error}</p>;
  }

  if (!profile) {
    return <p className="muted">Profile not found.</p>;
  }

  const isMe = userId === profile.id;

  const endpointByTab = {
    posts: `/api/users/${profile.id}/posts?page=0&size=20`,
    liked: "/api/users/me/liked-posts?page=0&size=20",
    commented: "/api/users/me/commented-posts?page=0&size=20",
  };

  const emptyByTab = {
    posts: isMe ? "You have not posted yet." : "No posts by this user yet.",
    liked: "You have not liked any posts yet.",
    commented: "You have not commented on any posts yet.",
  };

  return (
    <section className="stack-lg">
      <div className="card profile-head">
        <h1>@{profile.username}</h1>
        <p className="muted">
          Followers: {profile.followerCount} | Following: {profile.followingCount}
        </p>
        {isAuthenticated && !isMe && (
          <button className={profile.followedByMe ? "btn ghost" : "btn primary"} onClick={toggleFollow}>
            {profile.followedByMe ? "Unfollow" : "Follow"}
          </button>
        )}
      </div>

      {isMe ? (
        <>
          <div className="tabs card" style={{ justifyContent: "center" }}>
            <button className={tab === "posts" ? "tab active" : "tab"} onClick={() => setTab("posts")}>
              My Posts
            </button>
            <button className={tab === "liked" ? "tab active" : "tab"} onClick={() => setTab("liked")}>
              Liked
            </button>
            <button className={tab === "commented" ? "tab active" : "tab"} onClick={() => setTab("commented")}>
              Commented
            </button>
          </div>
          <FeedList endpoint={endpointByTab[tab]} emptyText={emptyByTab[tab]} reloadToken={`${reloadToken}-${tab}`} />
        </>
      ) : (
        <>
          <h2 className="section-title">Posts</h2>
          <FeedList endpoint={endpointByTab.posts} emptyText={emptyByTab.posts} reloadToken={reloadToken} />
        </>
      )}
    </section>
  );
}
