import {
    apiRequest,
    createLogger,
    getToken,
    getRoleFromToken,
    normalizeTokenValue,
    resolveHomeByRole,
    setSessionBadge,
    setToken
} from "/ui/common.js";

const elements = {
    sessionBadge: document.getElementById("sessionBadge"),
    logOutput: document.getElementById("logOutput"),
    registerForm: document.getElementById("registerForm"),
    loginForm: document.getElementById("loginForm")
};

const logger = createLogger(elements.logOutput);

function redirectByTokenIfPresent() {
    const token = getToken();
    if (!token) {
        return;
    }
    const role = getRoleFromToken(token);
    if (!role) {
        return;
    }
    window.location.replace(resolveHomeByRole(role));
}

elements.registerForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    const formData = new FormData(elements.registerForm);

    const payload = {
        login: String(formData.get("login") || "").trim(),
        password: String(formData.get("password") || ""),
        name: String(formData.get("name") || "").trim(),
        age: Number(formData.get("age")),
        sex: String(formData.get("sex") || "").toUpperCase(),
        hairColor: String(formData.get("hairColor") || "").toUpperCase()
    };

    try {
        await apiRequest("/users/register", {method: "POST", auth: false, body: payload});
        logger.info(`Пользователь ${payload.login} зарегистрирован`);
        elements.loginForm.username.value = payload.login;
        elements.loginForm.password.value = payload.password;
    } catch (error) {
        logger.error("Ошибка регистрации", error.message);
    }
});

elements.loginForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    const formData = new FormData(elements.loginForm);

    const payload = {
        username: String(formData.get("username") || "").trim(),
        password: String(formData.get("password") || "")
    };

    try {
        const response = await apiRequest("/login", {method: "POST", auth: false, body: payload});
        const token = normalizeTokenValue(response);
        if (!token) {
            throw new Error("Пустой JWT в ответе /login");
        }

        setToken(token);
        setSessionBadge(elements.sessionBadge, token);
        const role = getRoleFromToken(token);
        logger.info(`Вход выполнен: ${payload.username} (${role || "UNKNOWN"})`);
        window.location.replace(resolveHomeByRole(role));
    } catch (error) {
        logger.error("Ошибка логина", error.message);
    }
});

setSessionBadge(elements.sessionBadge, getToken());
logger.info("Готово. Выполни вход или регистрацию.");
redirectByTokenIfPresent();
