import { useEffect } from "react";

/**
 * A non-blocking in-app confirmation dialog.
 * Props:
 *   open        – whether to show it
 *   title       – bold heading
 *   message     – body text
 *   onConfirm   – called when user clicks the confirm button
 *   onCancel    – called when user clicks cancel or presses Escape
 *   confirmText – label for the confirm button (default "Delete")
 *   danger      – if true, confirm button is styled red
 */
export default function ConfirmDialog({
  open,
  title,
  message,
  onConfirm,
  onCancel,
  confirmText = "Delete",
  danger = true,
}) {
  // Close on Escape
  useEffect(() => {
    if (!open) return;
    const handler = (e) => { if (e.key === "Escape") onCancel(); };
    window.addEventListener("keydown", handler);
    return () => window.removeEventListener("keydown", handler);
  }, [open, onCancel]);

  if (!open) return null;

  return (
    <div className="dialog-backdrop" onClick={onCancel}>
      <div className="dialog-box" onClick={(e) => e.stopPropagation()} role="dialog" aria-modal="true">
        <h3 className="dialog-title">{title}</h3>
        <p className="dialog-body">{message}</p>
        <div className="dialog-actions">
          <button className="btn ghost" onClick={onCancel}>Cancel</button>
          <button className={`btn ${danger ? "danger-btn" : "primary"}`} onClick={onConfirm}>
            {confirmText}
          </button>
        </div>
      </div>
    </div>
  );
}
