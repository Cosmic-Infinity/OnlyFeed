import { useEffect, useState } from "react";
import { Heart, MessageSquare, SendHorizontal, Trash2 } from "lucide-react";
import { Link, useNavigate } from "react-router-dom";
import { apiRequest } from "../api/http";
import { useAuth } from "../context/AuthContext";
import { formatDateTime } from "../utils/date";
import ConfirmDialog from "./ConfirmDialog";

export default function PostCard({ post, onChanged }) {
  const { token, isAuthenticated, userId } = useAuth();
  const navigate = useNavigate();

  const [liked, setLiked] = useState(post.likedByMe);
  const [likeCount, setLikeCount] = useState(post.likeCount);
  const [showComments, setShowComments] = useState(false);
  const [comments, setComments] = useState([]);
  const [commentText, setCommentText] = useState("");
  const [commentError, setCommentError] = useState("");
  const [commentsLoading, setCommentsLoading] = useState(false);

  // Confirm dialog state
  const [confirmDialog, setConfirmDialog] = useState({ open: false, type: null, commentId: null });
  // Inline action error
  const [actionError, setActionError] = useState("");

  useEffect(() => {
    setLiked(post.likedByMe);
    setLikeCount(post.likeCount);
  }, [post.likedByMe, post.likeCount]);

  const toggleLike = async () => {
    if (!isAuthenticated) {
      navigate("/login");
      return;
    }

    const wasLiked = liked;
    setLiked(!wasLiked);
    setLikeCount((current) => current + (wasLiked ? -1 : 1));

    try {
      await apiRequest(`/api/posts/${post.id}/likes`, {
        method: wasLiked ? "DELETE" : "POST",
        token,
      });
      onChanged?.();
    } catch {
      setLiked(wasLiked);
      setLikeCount((current) => current + (wasLiked ? 1 : -1));
    }
  };

  const loadComments = async () => {
    setCommentsLoading(true);
    try {
      const page = await apiRequest(`/api/posts/${post.id}/comments?page=0&size=20`);
      setComments(page.content || []);
      setCommentError("");
    } catch (err) {
      setCommentError(err.message);
    } finally {
      setCommentsLoading(false);
    }
  };

  const toggleComments = async () => {
    const next = !showComments;
    setShowComments(next);
    if (next && comments.length === 0) {
      await loadComments();
    }
  };

  // --- Delete handlers via in-app dialog ---
  const confirmDeletePost = () => setConfirmDialog({ open: true, type: "post", commentId: null });
  const confirmDeleteComment = (commentId) => setConfirmDialog({ open: true, type: "comment", commentId });

  const handleConfirm = async () => {
    const { type, commentId } = confirmDialog;
    setConfirmDialog({ open: false, type: null, commentId: null });
    setActionError("");
    try {
      if (type === "post") {
        await apiRequest(`/api/posts/${post.id}`, { method: "DELETE", token });
        onChanged?.();
      } else if (type === "comment") {
        await apiRequest(`/api/comments/${commentId}`, { method: "DELETE", token });
        setComments((current) => current.filter((c) => c.id !== commentId));
        onChanged?.();
      }
    } catch (err) {
      setActionError(err.message);
    }
  };

  const handleCancel = () => setConfirmDialog({ open: false, type: null, commentId: null });

  const addComment = async (event) => {
    event.preventDefault();
    if (!isAuthenticated) {
      navigate("/login");
      return;
    }

    if (!commentText.trim()) {
      setCommentError("Comment cannot be empty.");
      return;
    }

    setCommentError("");
    try {
      const created = await apiRequest(`/api/posts/${post.id}/comments`, {
        method: "POST",
        token,
        body: { content: commentText.trim() },
      });
      setComments((current) => [created, ...current]);
      setCommentText("");
      onChanged?.();
    } catch (err) {
      setCommentError(err.message);
    }
  };

  const isDeleted = post.content === "[This post was deleted]";

  return (
    <>
      <ConfirmDialog
        open={confirmDialog.open}
        title={confirmDialog.type === "post" ? "Delete post?" : "Delete comment?"}
        message={
          confirmDialog.type === "post"
            ? "This will permanently remove your post content. Comments from others will be preserved."
            : "This will permanently remove your comment."
        }
        onConfirm={handleConfirm}
        onCancel={handleCancel}
      />

      <article className="card post-card">
        <div className="post-header">
          <Link to={`/profile/${post.author.username}`} className="post-author">
            @{post.author.username}
          </Link>
          <span className="post-date">{formatDateTime(post.createdAt)}</span>
        </div>

        {isDeleted ? (
          <p className="post-content muted" style={{ fontStyle: "italic" }}>
            [This post was deleted]
          </p>
        ) : (
          <p className="post-content">{post.content}</p>
        )}

        {actionError && <p className="inline-error" style={{ marginBottom: "0.5rem" }}>{actionError}</p>}

        <div className="post-actions">
          <button className={liked ? "action danger" : "action"} onClick={toggleLike} type="button">
            <Heart className="icon icon-sm" aria-hidden="true" />
            <span>{liked ? "Unlike" : "Like"}</span>
            <span className="action-count">({likeCount})</span>
          </button>
          <button className="action" onClick={toggleComments} type="button">
            <MessageSquare className="icon icon-sm" aria-hidden="true" />
            <span>Comments</span>
            <span className="action-count">({comments.length || post.commentCount})</span>
          </button>
          {isAuthenticated && post.author.id === userId && !isDeleted && (
            <button className="action danger" onClick={confirmDeletePost} type="button" style={{ marginLeft: "auto" }}>
              <Trash2 className="icon icon-sm" aria-hidden="true" />
              <span>Delete</span>
            </button>
          )}
        </div>

        {showComments && (
          <section className="comments">
            {isAuthenticated && (
              <form onSubmit={addComment} className="comment-form">
                <textarea value={commentText} maxLength={280} onChange={(event) => setCommentText(event.target.value)} placeholder="Write a comment" />
                <button className="btn primary btn-icon" type="submit">
                  <SendHorizontal className="icon icon-sm" aria-hidden="true" />
                  <span>Comment</span>
                </button>
              </form>
            )}
            {commentError && <p className="inline-error">{commentError}</p>}

            {commentsLoading && <p className="muted">Loading comments...</p>}
            {!commentsLoading && comments.length === 0 && !commentError && <p className="muted">No comments yet.</p>}

            <div className="comment-list">
              {comments.map((comment) => (
                <div key={comment.id} className="comment-item">
                  <div className="comment-header">
                    <Link to={`/profile/${comment.author.username}`}>@{comment.author.username}</Link>
                    <div style={{ display: "flex", alignItems: "center", gap: "0.5rem" }}>
                      <span className="comment-date">{formatDateTime(comment.createdAt)}</span>
                      {isAuthenticated && comment.author.id === userId && (
                        <button
                          className="btn ghost btn-icon"
                          onClick={() => confirmDeleteComment(comment.id)}
                          title="Delete comment"
                          style={{ padding: "0.2rem", color: "var(--danger)", border: "none" }}
                        >
                          <Trash2 className="icon icon-sm" aria-hidden="true" />
                        </button>
                      )}
                    </div>
                  </div>
                  <p>{comment.content}</p>
                </div>
              ))}
            </div>
          </section>
        )}
      </article>
    </>
  );
}
