import { Home, LogIn, LogOut, UserCircle2, UserPlus } from "lucide-react";
import { Link, NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
  const { isAuthenticated, username, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  return (
    <header className="navbar-wrap">
      <div className="navbar">
        <Link to="/" className="brand">
          OnlyFeed
        </Link>

        <nav className="nav-links">
          <NavLink to="/" end>
            <span className="nav-link-inner">
              <Home className="icon icon-sm" aria-hidden="true" />
              <span>Home</span>
            </span>
          </NavLink>
        </nav>

        <div className="auth-links">
          {isAuthenticated ? (
            <>
              <Link className="link-chip" to={`/profile/${username}`}>
                <UserCircle2 className="icon icon-sm" aria-hidden="true" />@{username}
              </Link>
              <button className="btn ghost btn-icon" onClick={handleLogout} title="Logout" style={{ padding: "0.5rem" }}>
                <LogOut className="icon icon-sm" aria-hidden="true" />
              </button>
            </>
          ) : (
            <>
              <Link className="btn ghost btn-icon" to="/login">
                <LogIn className="icon icon-sm" aria-hidden="true" />
                <span>Login</span>
              </Link>
              <Link className="btn primary btn-icon" to="/register">
                <UserPlus className="icon icon-sm" aria-hidden="true" />
                <span>Sign up</span>
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
