import { useEffect, useState } from "react";
import {
  Users,
  FileText,
  MessageSquare,
  Trash2,
  RefreshCw,
  LogOut,
  Zap,
  AlertTriangle,
  CheckCircle,
  ShieldAlert,
  UserPlus,
  Plus,
} from "lucide-react";
import { apiRequest } from "./api/http";

const AUTH_KEY = "onlyfeed-godmode-auth";

function readStoredAuth() {
  try {
    const raw = localStorage.getItem(AUTH_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

// ── Confirm dialog ────────────────────────────
function ConfirmDialog({ open, title, message, onConfirm, onCancel }) {
  useEffect(() => {
    if (!open) return;
    const handler = (e) => { if (e.key === "Escape") onCancel(); };
    window.addEventListener("keydown", handler);
    return () => window.removeEventListener("keydown", handler);
  }, [open, onCancel]);

  if (!open) return null;
  return (
    <div className="dialog-backdrop" onClick={onCancel}>
      <div className="dialog-box" onClick={(e) => e.stopPropagation()}>
        <div className="dialog-icon-row">
          <AlertTriangle size={20} className="dialog-warn-icon" />
          <h3>{title}</h3>
        </div>
        <p>{message}</p>
        <div className="dialog-actions">
          <button className="btn ghost" onClick={onCancel}>Cancel</button>
          <button className="btn danger" onClick={onConfirm}>
            <Trash2 size={14} /> Confirm delete
          </button>
        </div>
      </div>
    </div>
  );
}

// ── Auth screen ───────────────────────────────
function AuthScreen({ onLogin, error, loading }) {
  const [form, setForm] = useState({ username: "", password: "" });

  return (
    <div className="auth-bg">
      <div className="auth-card">
        <div className="auth-logo">
          <div className="auth-logo-icon"><Zap size={20} strokeWidth={2.5} /></div>
          <div className="auth-logo-text">
            <span className="auth-logo-name">OnlyFeed</span>
            <span className="auth-logo-sub">Godmode</span>
          </div>
        </div>

        <div>
          <h2>Admin sign in</h2>
          <p className="sub">Restricted access. Authorised personnel only.</p>
        </div>

        <form className="stack" onSubmit={(e) => { e.preventDefault(); onLogin(form); }}>
          <label>
            Username
            <input value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} autoComplete="username" required placeholder="admin" />
          </label>
          <label>
            Password
            <input type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} autoComplete="current-password" required placeholder="••••••••" />
          </label>
          {error && (
            <div className="alert alert-error">
              <AlertTriangle size={15} /> {error}
            </div>
          )}
          <button className="btn primary" type="submit" disabled={loading} style={{ marginTop: "0.25rem" }}>
            <ShieldAlert size={15} />
            {loading ? "Signing in…" : "Enter dashboard"}
          </button>
        </form>
      </div>
    </div>
  );
}

// ── Users tab ─────────────────────────────────
function UsersTab({ users, token, onRefresh, loading }) {
  const [createForm, setCreateForm] = useState({ username: "", password: "" });
  const [message, setMessage] = useState(null);
  const [confirm, setConfirm] = useState(null);

  const createUser = async (e) => {
    e.preventDefault();
    setMessage(null);
    try {
      await apiRequest("/api/admin/users", { method: "POST", token, body: createForm });
      setMessage({ type: "success", text: `User @${createForm.username} created.` });
      setCreateForm({ username: "", password: "" });
      onRefresh();
    } catch (err) {
      setMessage({ type: "error", text: err.message });
    }
  };

  const doDelete = async () => {
    const { userId } = confirm;
    setConfirm(null);
    try {
      await apiRequest(`/api/admin/users/${userId}`, { method: "DELETE", token });
      onRefresh();
    } catch (err) {
      setMessage({ type: "error", text: err.message });
    }
  };

  return (
    <>
      <ConfirmDialog open={!!confirm} title="Delete user?" message="This will permanently remove the user and all their content. This cannot be undone." onConfirm={doDelete} onCancel={() => setConfirm(null)} />

      <div className="panel">
        <div className="panel-header">
          <h2><UserPlus size={16} style={{ marginRight: "0.4rem", verticalAlign: "middle" }} />Create user</h2>
        </div>
        <div className="panel-body">
          <form className="form-grid" onSubmit={createUser}>
            <label>Username<input value={createForm.username} onChange={(e) => setCreateForm({ ...createForm, username: e.target.value })} required placeholder="username" /></label>
            <label>Password<input type="password" value={createForm.password} onChange={(e) => setCreateForm({ ...createForm, password: e.target.value })} required placeholder="••••••••" /></label>
            <button className="btn primary" type="submit" style={{ alignSelf: "end" }}><Plus size={15} /> Create</button>
          </form>
          {message && (
            <div className={`alert ${message.type === "success" ? "alert-success" : "alert-error"}`} style={{ marginTop: "1rem" }}>
              {message.type === "success" ? <CheckCircle size={15} /> : <AlertTriangle size={15} />}
              {message.text}
            </div>
          )}
        </div>
      </div>

      <div className="panel">
        <div className="panel-header">
          <h2><Users size={16} style={{ marginRight: "0.4rem", verticalAlign: "middle" }} />All users</h2>
          <span className="muted text-sm">{users.length} accounts</span>
        </div>
        {loading ? (
          <div className="panel-body">{[...Array(4)].map((_, i) => <div key={i} className="skeleton-row" />)}</div>
        ) : users.length === 0 ? (
          <div className="empty-state"><Users size={36} className="empty-icon-svg" /><p>No users found.</p></div>
        ) : (
          <table className="data-table">
            <thead><tr><th>User</th><th>Role</th><th>Joined</th><th></th></tr></thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id}>
                  <td>
                    <div className="cell-main">@{user.username}</div>
                    <div className="cell-sub">ID #{user.id}</div>
                  </td>
                  <td><span className={`badge ${user.role === "ADMIN" ? "badge-admin" : "badge-user"}`}>{user.role}</span></td>
                  <td className="cell-sub">{new Date(user.createdAt).toLocaleDateString("en-GB", { day: "numeric", month: "short", year: "numeric" })}</td>
                  <td className="cell-actions">
                    <button className="btn danger btn-sm" onClick={() => setConfirm({ userId: user.id })}>
                      <Trash2 size={13} /> Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </>
  );
}

// ── Posts tab ─────────────────────────────────
function PostsTab({ posts, token, onRefresh, loading }) {
  const [confirm, setConfirm] = useState(null);
  const [error, setError] = useState("");

  const doDelete = async () => {
    const { postId } = confirm;
    setConfirm(null);
    setError("");
    try {
      await apiRequest(`/api/admin/posts/${postId}`, { method: "DELETE", token });
      onRefresh();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <>
      <ConfirmDialog open={!!confirm} title="Delete post?" message="This will permanently remove the post and all associated comments and likes." onConfirm={doDelete} onCancel={() => setConfirm(null)} />
      <div className="panel">
        <div className="panel-header">
          <h2><FileText size={16} style={{ marginRight: "0.4rem", verticalAlign: "middle" }} />All posts</h2>
          <span className="muted text-sm">{posts.length} posts</span>
        </div>
        {error && <div className="alert alert-error" style={{ margin: "0 1.5rem 1rem" }}><AlertTriangle size={15} /> {error}</div>}
        {loading ? (
          <div className="panel-body">{[...Array(4)].map((_, i) => <div key={i} className="skeleton-row" />)}</div>
        ) : posts.length === 0 ? (
          <div className="empty-state"><FileText size={36} className="empty-icon-svg" /><p>No posts found.</p></div>
        ) : (
          <table className="data-table">
            <thead><tr><th>Post</th><th>Content</th><th>Interactions</th><th></th></tr></thead>
            <tbody>
              {posts.map((post) => (
                <tr key={post.id}>
                  <td>
                    <div className="cell-main">@{post.authorUsername}</div>
                    <div className="cell-sub">#{post.id} · {new Date(post.createdAt).toLocaleDateString("en-GB", { day: "numeric", month: "short" })}</div>
                  </td>
                  <td><div className="cell-content">{post.content}</div></td>
                  <td className="cell-sub">{post.likeCount} likes · {post.commentCount} comments</td>
                  <td className="cell-actions">
                    <button className="btn danger btn-sm" onClick={() => setConfirm({ postId: post.id })}>
                      <Trash2 size={13} /> Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </>
  );
}

// ── Comments tab ──────────────────────────────
function CommentsTab({ comments, token, onRefresh, loading }) {
  const [confirm, setConfirm] = useState(null);
  const [error, setError] = useState("");

  const doDelete = async () => {
    const { commentId } = confirm;
    setConfirm(null);
    setError("");
    try {
      await apiRequest(`/api/admin/comments/${commentId}`, { method: "DELETE", token });
      onRefresh();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <>
      <ConfirmDialog open={!!confirm} title="Delete comment?" message="This will permanently remove the comment." onConfirm={doDelete} onCancel={() => setConfirm(null)} />
      <div className="panel">
        <div className="panel-header">
          <h2><MessageSquare size={16} style={{ marginRight: "0.4rem", verticalAlign: "middle" }} />All comments</h2>
          <span className="muted text-sm">{comments.length} comments</span>
        </div>
        {error && <div className="alert alert-error" style={{ margin: "0 1.5rem 1rem" }}><AlertTriangle size={15} /> {error}</div>}
        {loading ? (
          <div className="panel-body">{[...Array(4)].map((_, i) => <div key={i} className="skeleton-row" />)}</div>
        ) : comments.length === 0 ? (
          <div className="empty-state"><MessageSquare size={36} className="empty-icon-svg" /><p>No comments found.</p></div>
        ) : (
          <table className="data-table">
            <thead><tr><th>Author</th><th>Comment</th><th>Post</th><th></th></tr></thead>
            <tbody>
              {comments.map((comment) => (
                <tr key={comment.id}>
                  <td>
                    <div className="cell-main">@{comment.authorUsername}</div>
                    <div className="cell-sub">#{comment.id}</div>
                  </td>
                  <td><div className="cell-content">{comment.content}</div></td>
                  <td className="cell-sub">Post #{comment.postId}</td>
                  <td className="cell-actions">
                    <button className="btn danger btn-sm" onClick={() => setConfirm({ commentId: comment.id })}>
                      <Trash2 size={13} /> Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </>
  );
}

// ── Dashboard shell ───────────────────────────
function Dashboard({ auth, onLogout }) {
  const [tab, setTab] = useState("users");
  const [users, setUsers] = useState([]);
  const [posts, setPosts] = useState([]);
  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [globalError, setGlobalError] = useState("");
  const [refreshToken, setRefreshToken] = useState(0);

  const token = auth?.token;

  useEffect(() => {
    let cancelled = false;
    const load = async () => {
      setLoading(true);
      setGlobalError("");
      try {
        const [userPage, postPage, commentPage] = await Promise.all([
          apiRequest("/api/admin/users?page=0&size=50", { token }),
          apiRequest("/api/admin/posts?page=0&size=50", { token }),
          apiRequest("/api/admin/comments?page=0&size=50", { token }),
        ]);
        if (!cancelled) {
          setUsers(userPage.content || []);
          setPosts(postPage.content || []);
          setComments(commentPage.content || []);
        }
      } catch (err) {
        if (!cancelled) setGlobalError(err.message);
      } finally {
        if (!cancelled) setLoading(false);
      }
    };
    load();
    return () => { cancelled = true; };
  }, [token, refreshToken]);

  const refresh = () => setRefreshToken((v) => v + 1);

  const navItems = [
    { id: "users",    label: "Users",    Icon: Users,         count: users.length },
    { id: "posts",    label: "Posts",    Icon: FileText,      count: posts.length },
    { id: "comments", label: "Comments", Icon: MessageSquare, count: comments.length },
  ];

  const statItems = [
    { label: "Total Users",    value: users.length,    Icon: Users,         cls: "blue" },
    { label: "Total Posts",    value: posts.length,    Icon: FileText,      cls: "green" },
    { label: "Total Comments", value: comments.length, Icon: MessageSquare, cls: "red" },
  ];

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="sidebar-logo">
          <div className="sidebar-logo-icon"><Zap size={18} strokeWidth={2.5} /></div>
          <div className="sidebar-logo-text">
            <span className="sidebar-logo-name">OnlyFeed</span>
            <span className="sidebar-logo-sub">Godmode</span>
          </div>
        </div>

        <p className="sidebar-section-label">Moderation</p>
        <nav>
          {navItems.map(({ id, label, Icon, count }) => (
            <button key={id} className={`nav-item ${tab === id ? "active" : ""}`} onClick={() => setTab(id)}>
              <Icon size={15} />
              <span>{label}</span>
              {!loading && <span className="nav-item-badge">{count}</span>}
            </button>
          ))}
        </nav>

        <div className="sidebar-footer">
          <div className="signed-in-as">
            Signed in as
            <strong>@{auth.username}</strong>
          </div>
          <button className="btn ghost" style={{ width: "100%", marginTop: "0.5rem", justifyContent: "center" }} onClick={onLogout}>
            <LogOut size={14} /> Sign out
          </button>
        </div>
      </aside>

      <main className="main-content">
        <div className="page-header">
          <div>
            <h1>{navItems.find((n) => n.id === tab)?.label ?? "Dashboard"}</h1>
            <p className="sub">Manage and moderate platform content.</p>
          </div>
          <button className="btn ghost btn-sm" onClick={refresh}>
            <RefreshCw size={13} /> Refresh
          </button>
        </div>

        <div className="stats-row">
          {statItems.map(({ label, value, Icon, cls }) => (
            <div className="stat-card" key={label}>
              <div className={`stat-icon ${cls}`}><Icon size={20} /></div>
              <div>
                <div className="stat-value">{value}</div>
                <div className="stat-label">{label}</div>
              </div>
            </div>
          ))}
        </div>

        {globalError && <div className="alert alert-error"><AlertTriangle size={15} /> {globalError}</div>}

        {tab === "users"    && <UsersTab    users={users}       token={token} onRefresh={refresh} loading={loading} />}
        {tab === "posts"    && <PostsTab    posts={posts}       token={token} onRefresh={refresh} loading={loading} />}
        {tab === "comments" && <CommentsTab comments={comments} token={token} onRefresh={refresh} loading={loading} />}
      </main>
    </div>
  );
}

// ── Root ──────────────────────────────────────
export default function App() {
  const [auth, setAuth] = useState(() => readStoredAuth());
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const login = async (form) => {
    setLoading(true);
    setError("");
    try {
      const result = await apiRequest("/api/auth/login", { method: "POST", body: form });
      if (result.role !== "ADMIN") throw new Error("Admin access required.");
      setAuth(result);
      localStorage.setItem(AUTH_KEY, JSON.stringify(result));
    } catch (err) {
      setAuth(null);
      localStorage.removeItem(AUTH_KEY);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    setAuth(null);
    localStorage.removeItem(AUTH_KEY);
  };

  return auth
    ? <Dashboard auth={auth} onLogout={logout} />
    : <AuthScreen onLogin={login} error={error} loading={loading} />;
}
