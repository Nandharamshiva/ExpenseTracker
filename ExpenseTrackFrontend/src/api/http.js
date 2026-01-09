const DEFAULT_BASE_URL = '';

function buildUrl(path, query) {
  const url = new URL(path, window.location.origin);
  // When using Vite dev proxy, baseUrl is empty and /api routes proxy to backend.
  // For production, set VITE_API_BASE_URL and we will use absolute URLs.
  const baseUrl = import.meta.env.VITE_API_BASE_URL;
  if (baseUrl && baseUrl.trim().length > 0) {
    return new URL(path + (query ? `?${query}` : ''), baseUrl).toString();
  }
  if (query) url.search = query;
  return url.pathname + url.search;
}

export async function apiRequest(path, { method = 'GET', token, body, query, headers } = {}) {
  const finalHeaders = {
    Accept: 'application/json',
    ...(body ? { 'Content-Type': 'application/json' } : {}),
    ...(headers || {}),
  };

  if (token) {
    finalHeaders.Authorization = `Bearer ${token}`;
  }

  const res = await fetch(buildUrl(path, query), {
    method,
    headers: finalHeaders,
    body: body ? JSON.stringify(body) : undefined,
  });

  const contentType = res.headers.get('content-type') || '';
  const isJson = contentType.includes('application/json');

  if (res.status === 204) {
    return { ok: true, status: res.status, data: null };
  }

  const data = isJson ? await res.json().catch(() => null) : await res.text().catch(() => null);

  if (!res.ok) {
    const message =
      (data && typeof data === 'object' && (data.message || data.error)) ||
      (typeof data === 'string' && data) ||
      `Request failed (${res.status})`;

    const err = new Error(message);
    err.status = res.status;
    err.data = data;
    throw err;
  }

  return { ok: true, status: res.status, data };
}

export function toQuery(params) {
  const search = new URLSearchParams();
  Object.entries(params || {}).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') return;
    search.set(key, String(value));
  });
  const s = search.toString();
  return s.length ? s : undefined;
}
