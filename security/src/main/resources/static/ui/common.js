const TOKEN_STORAGE_KEY = "bankapp_ui_token";

const numberFormatter = new Intl.NumberFormat("ru-RU", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
});

function timestamp() {
    return new Date().toLocaleTimeString("ru-RU", {hour12: false});
}

function decodeBase64Url(value) {
    const normalized = value.replace(/-/g, "+").replace(/_/g, "/");
    const paddingLength = (4 - (normalized.length % 4)) % 4;
    const padding = "=".repeat(paddingLength);
    return atob(normalized + padding);
}

function parseJsonSafe(raw) {
    if (typeof raw !== "string" || raw.length === 0) {
        return raw;
    }
    try {
        return JSON.parse(raw);
    } catch (_error) {
        return raw;
    }
}

function extractErrorMessage(payload, fallback) {
    if (payload && typeof payload === "object") {
        if (typeof payload.message === "string" && payload.message.trim() !== "") {
            return payload.message;
        }
        if (typeof payload.error === "string" && payload.error.trim() !== "") {
            return payload.error;
        }
    }

    if (typeof payload === "string" && payload.trim() !== "") {
        return payload.trim();
    }
    return fallback;
}

function normalizeResponseBody(contentType, rawBody) {
    if (!rawBody) {
        return null;
    }
    if (contentType.includes("application/json")) {
        return parseJsonSafe(rawBody);
    }

    const trimmed = rawBody.trim();
    if ((trimmed.startsWith("{") && trimmed.endsWith("}")) || (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
        return parseJsonSafe(trimmed);
    }

    return rawBody;
}

export function getToken() {
    return localStorage.getItem(TOKEN_STORAGE_KEY) || "";
}

export function setToken(token) {
    localStorage.setItem(TOKEN_STORAGE_KEY, token);
}

export function clearToken() {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
}

export function decodeJwtPayload(token) {
    if (!token || typeof token !== "string") {
        return null;
    }
    const parts = token.split(".");
    if (parts.length < 2) {
        return null;
    }
    try {
        const decoded = decodeBase64Url(parts[1]);
        return JSON.parse(decoded);
    } catch (_error) {
        return null;
    }
}

export function getRoleFromToken(token = getToken()) {
    const payload = decodeJwtPayload(token);
    if (!payload || typeof payload.role !== "string") {
        return "";
    }
    return payload.role.toUpperCase();
}

export function getUsernameFromToken(token = getToken()) {
    const payload = decodeJwtPayload(token);
    return payload && typeof payload.sub === "string" ? payload.sub : "";
}

export function getUserIdFromToken(token = getToken()) {
    const payload = decodeJwtPayload(token);
    return payload && typeof payload.userId === "string" ? payload.userId : "";
}

export function resolveHomeByRole(role) {
    return role === "ADMIN" ? "/ui/admin.html" : "/ui/client.html";
}

export function setSessionBadge(element, token = getToken()) {
    if (!element) {
        return;
    }
    if (!token) {
        element.textContent = "Не авторизован";
        element.classList.remove("session-on");
        element.classList.add("session-off");
        return;
    }

    const role = getRoleFromToken(token) || "UNKNOWN";
    const username = getUsernameFromToken(token) || "unknown";
    element.textContent = `${username} (${role})`;
    element.classList.remove("session-off");
    element.classList.add("session-on");
}

export function ensureRoleOrRedirect(allowedRoles) {
    const token = getToken();
    if (!token) {
        window.location.replace("/ui/login.html");
        return null;
    }

    const role = getRoleFromToken(token);
    if (!role) {
        clearToken();
        window.location.replace("/ui/login.html");
        return null;
    }

    if (Array.isArray(allowedRoles) && allowedRoles.length > 0 && !allowedRoles.includes(role)) {
        window.location.replace(resolveHomeByRole(role));
        return null;
    }

    return role;
}

export function buildQuery(params) {
    const query = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
        if (value === null || value === undefined) {
            return;
        }
        const text = String(value).trim();
        if (text !== "") {
            query.set(key, text);
        }
    });
    return query.toString();
}

export async function apiRequest(path, options = {}) {
    const method = options.method || "GET";
    const auth = options.auth !== false;
    const headers = {
        Accept: "application/json, text/plain, */*",
        ...(options.headers || {})
    };

    if (options.body !== undefined) {
        headers["Content-Type"] = "application/json";
    }

    if (auth) {
        const token = getToken();
        if (!token) {
            throw new Error("Нужна авторизация. Выполни вход.");
        }
        headers.Authorization = `Bearer ${token}`;
    }

    const response = await fetch(path, {
        method,
        headers,
        body: options.body !== undefined ? JSON.stringify(options.body) : undefined
    });

    const rawBody = await response.text();
    const contentType = response.headers.get("content-type") || "";
    const payload = normalizeResponseBody(contentType, rawBody);

    if (!response.ok) {
        const message = extractErrorMessage(payload, response.statusText);
        throw new Error(`[${response.status}] ${message}`);
    }

    return payload;
}

export function normalizeTokenValue(payload) {
    if (payload === null || payload === undefined) {
        return "";
    }
    if (typeof payload === "string") {
        return payload.trim().replace(/^"+|"+$/g, "");
    }
    return String(payload).trim();
}

export function createLogger(outputElement) {
    const write = (message, details) => {
        if (!outputElement) {
            return;
        }

        const line = `[${timestamp()}] ${message}`;
        const body = details === undefined
                ? line
                : `${line}\n${typeof details === "string" ? details : JSON.stringify(details, null, 2)}`;
        outputElement.textContent = `${body}\n\n${outputElement.textContent || ""}`.trim();
    };

    return {
        info: (message, details) => write(message, details),
        error: (message, details) => write(message, details)
    };
}

export function formatMoney(value) {
    if (value === null || value === undefined || value === "") {
        return "0.00";
    }
    const parsed = Number(value);
    if (Number.isNaN(parsed)) {
        return String(value);
    }
    return numberFormatter.format(parsed);
}

export function formatDateTime(value) {
    if (!value) {
        return "-";
    }
    try {
        return new Date(value).toLocaleString("ru-RU", {hour12: false});
    } catch (_error) {
        return String(value);
    }
}

export function renderEmptyRow(tbody, colSpan, message) {
    tbody.innerHTML = "";
    const row = document.createElement("tr");
    const cell = document.createElement("td");
    cell.colSpan = colSpan;
    cell.className = "empty-cell";
    cell.textContent = message;
    row.appendChild(cell);
    tbody.appendChild(row);
}

export function isUuid(value) {
    if (typeof value !== "string") {
        return false;
    }
    return /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(value.trim());
}

export async function performLogout(logger) {
    try {
        await apiRequest("/logout", {method: "POST"});
    } catch (error) {
        if (logger) {
            logger.error("Logout завершен с ошибкой", error.message);
        }
    } finally {
        clearToken();
    }
}
