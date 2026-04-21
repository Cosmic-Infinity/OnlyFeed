const API_BASE = import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, "") || "";

function buildPlayfulError({ status, path, serverMessage, traceId }) {
  if (status >= 500) {
    const trace = traceId ? ` Trace ID: ${traceId}.` : "";
    return `The server pulled a dramatic fainting scene while handling ${path}. Please ping the developer squad with this clue.${trace} Technical note: ${serverMessage || "Internal server error"}.`;
  }

  if (status === 404) {
    return `We looked everywhere, but ${path} is missing in action (404). If this should exist, please let the developer know.`;
  }

  if (status === 401 || status === 403) {
    return `This request needs special access vibes (${status}). Try logging in again, and if it still blocks you, share this with the developer: ${serverMessage || "Access denied"}.`;
  }

  if (status >= 400) {
    return `Your request hit a speed bump (${status}) at ${path}. Details: ${serverMessage || "Unknown client-side issue"}.`;
  }

  return serverMessage || "Unexpected request error";
}

export async function apiRequest(path, { method = "GET", token, body } = {}) {
  const headers = {
    "Content-Type": "application/json",
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const requestUrl = API_BASE ? `${API_BASE}${path}` : path;

  let response;
  try {
    response = await fetch(requestUrl, {
      method,
      headers,
      body: body ? JSON.stringify(body) : undefined,
    });
  } catch (err) {
    throw new Error("Network error: Could not connect to the server. Please check your internet connection or try again later.");
  }

  const contentType = response.headers.get("content-type") || "";
  const data = contentType.includes("application/json") ? await response.json() : null;

  if (!response.ok) {
    const message = buildPlayfulError({
      status: response.status,
      path,
      serverMessage: data?.error || data?.message,
      traceId: data?.traceId,
    });
    throw new Error(message);
  }

  return data;
}
