import {
    apiRequest,
    buildQuery,
    createLogger,
    ensureRoleOrRedirect,
    formatDateTime,
    formatMoney,
    isUuid,
    performLogout,
    renderEmptyRow,
    setSessionBadge
} from "/ui/common.js";

const elements = {
    sessionBadge: document.getElementById("sessionBadge"),
    logOutput: document.getElementById("logOutput"),
    refreshButton: document.getElementById("refreshButton"),
    logoutButton: document.getElementById("logoutButton"),
    usersFilterForm: document.getElementById("usersFilterForm"),
    userHairColorFilter: document.getElementById("userHairColorFilter"),
    userSexFilter: document.getElementById("userSexFilter"),
    userByIdForm: document.getElementById("userByIdForm"),
    userIdInput: document.getElementById("userIdInput"),
    usersTableBody: document.getElementById("usersTableBody"),
    selectedUserOutput: document.getElementById("selectedUserOutput"),
    accountsByUserForm: document.getElementById("accountsByUserForm"),
    accountsUserIdInput: document.getElementById("accountsUserIdInput"),
    loadAllAccountsButton: document.getElementById("loadAllAccountsButton"),
    accountByIdForm: document.getElementById("accountByIdForm"),
    accountIdInput: document.getElementById("accountIdInput"),
    accountsTableBody: document.getElementById("accountsTableBody"),
    selectedAccountOutput: document.getElementById("selectedAccountOutput"),
    transactionFilterForm: document.getElementById("transactionFilterForm"),
    transactionTypeFilter: document.getElementById("transactionTypeFilter"),
    transactionAccountIdFilter: document.getElementById("transactionAccountIdFilter"),
    transactionsTableBody: document.getElementById("transactionsTableBody"),
    eventFilterForm: document.getElementById("eventFilterForm"),
    eventSourceFilter: document.getElementById("eventSourceFilter"),
    eventTypeFilter: document.getElementById("eventTypeFilter"),
    eventEntityIdFilter: document.getElementById("eventEntityIdFilter"),
    eventCorrelationIdFilter: document.getElementById("eventCorrelationIdFilter"),
    eventTransactionTypeFilter: document.getElementById("eventTransactionTypeFilter"),
    eventLimitInput: document.getElementById("eventLimitInput"),
    eventsTableBody: document.getElementById("eventsTableBody")
};

const logger = createLogger(elements.logOutput);

const state = {
    users: [],
    allAccounts: [],
    shownAccounts: [],
    allTransactions: [],
    shownTransactions: [],
    events: []
};

function accountCountByOwner(accounts) {
    const map = new Map();
    (accounts || []).forEach((account) => {
        const ownerId = account?.ownerId;
        if (!ownerId) {
            return;
        }
        map.set(ownerId, (map.get(ownerId) || 0) + 1);
    });
    return map;
}

function transactionCountByAccount(transactions) {
    const map = new Map();
    (transactions || []).forEach((transaction) => {
        const accountId = transaction?.accountId;
        if (!accountId) {
            return;
        }
        map.set(accountId, (map.get(accountId) || 0) + 1);
    });
    return map;
}

function renderUsers(users, accounts) {
    if (!Array.isArray(users) || users.length === 0) {
        renderEmptyRow(elements.usersTableBody, 7, "Пользователи не найдены");
        return;
    }

    const accountsByOwner = accountCountByOwner(accounts);
    elements.usersTableBody.innerHTML = "";

    users.forEach((user) => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${user.id || "-"}</td>
            <td>${user.login || "-"}</td>
            <td>${user.name || "-"}</td>
            <td>${user.sex || "-"}</td>
            <td>${user.hairColor || "-"}</td>
            <td>${user.age ?? "-"}</td>
            <td>${accountsByOwner.get(user.id) || 0}</td>
        `;
        elements.usersTableBody.appendChild(row);
    });
}

function renderAccounts(accounts, transactions) {
    if (!Array.isArray(accounts) || accounts.length === 0) {
        renderEmptyRow(elements.accountsTableBody, 4, "Счета не найдены");
        return;
    }

    const txByAccount = transactionCountByAccount(transactions);
    elements.accountsTableBody.innerHTML = "";

    accounts.forEach((account) => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${account.accountId || "-"}</td>
            <td>${account.ownerId || "-"}</td>
            <td>${formatMoney(account.balance)}</td>
            <td>${txByAccount.get(account.accountId) || 0}</td>
        `;
        elements.accountsTableBody.appendChild(row);
    });
}

function renderTransactions(transactions) {
    if (!Array.isArray(transactions) || transactions.length === 0) {
        renderEmptyRow(elements.transactionsTableBody, 5, "Транзакции не найдены");
        return;
    }

    elements.transactionsTableBody.innerHTML = "";
    transactions.forEach((transaction) => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${formatDateTime(transaction.createdAt)}</td>
            <td>${transaction.transactionType || "-"}</td>
            <td>${formatMoney(transaction.amount)}</td>
            <td>${transaction.accountId || "-"}</td>
            <td>${transaction.transactionId || "-"}</td>
        `;
        elements.transactionsTableBody.appendChild(row);
    });
}

function renderEvents(events) {
    if (!Array.isArray(events) || events.length === 0) {
        renderEmptyRow(elements.eventsTableBody, 8, "События не найдены");
        return;
    }

    elements.eventsTableBody.innerHTML = "";
    events.forEach((event) => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${formatDateTime(event.eventTime)}</td>
            <td>${event.source || "-"}</td>
            <td>${event.eventType || "-"}</td>
            <td>${event.entityId || "-"}</td>
            <td>${event.transactionType || "-"}</td>
            <td>${event.amount == null ? "-" : formatMoney(event.amount)}</td>
            <td>${event.correlationId || "-"}</td>
            <td>${event.eventId || "-"}</td>
        `;
        elements.eventsTableBody.appendChild(row);
    });
}

function setJsonOutput(target, value, fallbackText) {
    target.textContent = value ? JSON.stringify(value, null, 2) : fallbackText;
}

function applyTransactionFilters() {
    const selectedType = String(elements.transactionTypeFilter.value || "").trim().toUpperCase();
    const selectedAccountId = String(elements.transactionAccountIdFilter.value || "").trim();

    if (selectedAccountId && !isUuid(selectedAccountId)) {
        throw new Error("Account ID в фильтре транзакций должен быть UUID");
    }

    state.shownTransactions = (state.allTransactions || []).filter((transaction) => {
        if (selectedType && String(transaction.transactionType || "").toUpperCase() !== selectedType) {
            return false;
        }
        if (selectedAccountId && String(transaction.accountId || "") !== selectedAccountId) {
            return false;
        }
        return true;
    });

    renderTransactions(state.shownTransactions);
}

function renderAll() {
    renderUsers(state.users, state.allAccounts);
    renderAccounts(state.shownAccounts, state.allTransactions);
    applyTransactionFilters();
    renderEvents(state.events);
}

async function loadUsers() {
    const query = buildQuery({
        hairColor: elements.userHairColorFilter.value,
        sex: elements.userSexFilter.value
    });
    const path = query ? `/users?${query}` : "/users";
    const users = await apiRequest(path);
    state.users = Array.isArray(users) ? users : [];
}

async function loadAllAccounts() {
    const accounts = await apiRequest("/accounts");
    state.allAccounts = Array.isArray(accounts) ? accounts : [];
    state.shownAccounts = state.allAccounts;
}

async function loadAllTransactions() {
    const transactions = await apiRequest("/transactions");
    state.allTransactions = Array.isArray(transactions) ? transactions : [];
}

function validateUuidFilterOrThrow(value, label) {
    if (!value) {
        return;
    }
    if (!isUuid(value)) {
        throw new Error(`${label} должен быть UUID`);
    }
}

function buildEventFiltersQuery() {
    const source = String(elements.eventSourceFilter.value || "").trim().toUpperCase();
    const eventType = String(elements.eventTypeFilter.value || "").trim();
    const entityId = String(elements.eventEntityIdFilter.value || "").trim();
    const correlationId = String(elements.eventCorrelationIdFilter.value || "").trim();
    const transactionType = String(elements.eventTransactionTypeFilter.value || "").trim().toUpperCase();
    const limitRaw = String(elements.eventLimitInput.value || "").trim();

    validateUuidFilterOrThrow(entityId, "Entity ID");
    validateUuidFilterOrThrow(correlationId, "Correlation ID");

    let limit = Number.parseInt(limitRaw || "100", 10);
    if (Number.isNaN(limit)) {
        limit = 100;
    }
    if (limit < 1 || limit > 500) {
        throw new Error("Limit должен быть от 1 до 500");
    }

    return buildQuery({
        source,
        eventType,
        entityId,
        correlationId,
        transactionType,
        limit
    });
}

async function loadEvents() {
    const query = buildEventFiltersQuery();
    const path = query ? `/events?${query}` : "/events";
    const events = await apiRequest(path);
    state.events = Array.isArray(events) ? events : [];
}

async function refreshAll() {
    await Promise.all([loadUsers(), loadAllAccounts(), loadAllTransactions(), loadEvents()]);
    renderAll();
}

elements.refreshButton.addEventListener("click", async () => {
    try {
        await refreshAll();
        logger.info("Данные админ-панели обновлены");
    } catch (error) {
        logger.error("Ошибка обновления", error.message);
    }
});

elements.logoutButton.addEventListener("click", async () => {
    await performLogout(logger);
    window.location.replace("/ui/login.html");
});

elements.usersFilterForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    try {
        await loadUsers();
        renderUsers(state.users, state.allAccounts);
        logger.info("Список пользователей обновлен");
    } catch (error) {
        logger.error("Ошибка загрузки пользователей", error.message);
    }
});

elements.userByIdForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    const userId = String(elements.userIdInput.value || "").trim();
    if (!isUuid(userId)) {
        logger.error("Неверный User ID", "Ожидается UUID");
        return;
    }

    try {
        const user = await apiRequest(`/users/${userId}`);
        setJsonOutput(elements.selectedUserOutput, user, "Пользователь не найден");
        logger.info(`Пользователь загружен: ${userId}`);
    } catch (error) {
        logger.error("Ошибка загрузки пользователя", error.message);
    }
});

elements.accountsByUserForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    const userId = String(elements.accountsUserIdInput.value || "").trim();
    if (!isUuid(userId)) {
        logger.error("Неверный User ID", "Ожидается UUID");
        return;
    }

    try {
        state.shownAccounts = state.allAccounts.filter((account) => String(account.ownerId || "") === userId);
        renderAccounts(state.shownAccounts, state.allTransactions);
        logger.info(`Счета пользователя ${userId} показаны`);
    } catch (error) {
        logger.error("Ошибка фильтрации счетов пользователя", error.message);
    }
});

elements.loadAllAccountsButton.addEventListener("click", async () => {
    try {
        state.shownAccounts = state.allAccounts;
        renderAccounts(state.shownAccounts, state.allTransactions);
        logger.info("Показаны все счета");
    } catch (error) {
        logger.error("Ошибка отображения счетов", error.message);
    }
});

elements.accountByIdForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    const accountId = String(elements.accountIdInput.value || "").trim();
    if (!isUuid(accountId)) {
        logger.error("Неверный Account ID", "Ожидается UUID");
        return;
    }

    try {
        const account = await apiRequest(`/accounts/${accountId}`);
        const accountTransactions = state.allTransactions.filter(
            (transaction) => String(transaction.accountId || "") === String(account.accountId || "")
        );
        const enriched = {
            ...account,
            transactionsCount: accountTransactions.length
        };
        setJsonOutput(elements.selectedAccountOutput, enriched, "Счет не найден");
        logger.info(`Счет загружен: ${accountId}`);
    } catch (error) {
        logger.error("Ошибка загрузки счета", error.message);
    }
});

elements.transactionFilterForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    try {
        applyTransactionFilters();
        logger.info("Список транзакций обновлен");
    } catch (error) {
        logger.error("Ошибка фильтрации транзакций", error.message);
    }
});

elements.eventFilterForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    try {
        await loadEvents();
        renderEvents(state.events);
        logger.info("Список событий обновлен");
    } catch (error) {
        logger.error("Ошибка загрузки событий", error.message);
    }
});

const role = ensureRoleOrRedirect(["ADMIN"]);
if (!role) {
    throw new Error("Unauthorized");
}

setSessionBadge(elements.sessionBadge);
logger.info("Админ-панель готова");

refreshAll()
    .then(() => logger.info("Первичная загрузка выполнена"))
    .catch((error) => logger.error("Ошибка первичной загрузки", error.message));
