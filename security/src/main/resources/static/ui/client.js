import {
    apiRequest,
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
    profileOutput: document.getElementById("profileOutput"),
    accountCountValue: document.getElementById("accountCountValue"),
    totalBalanceValue: document.getElementById("totalBalanceValue"),
    transactionsCountValue: document.getElementById("transactionsCountValue"),
    refreshButton: document.getElementById("refreshButton"),
    logoutButton: document.getElementById("logoutButton"),
    friendForm: document.getElementById("friendForm"),
    addFriendButton: document.getElementById("addFriendButton"),
    removeFriendButton: document.getElementById("removeFriendButton"),
    createAccountButton: document.getElementById("createAccountButton"),
    loadAccountsButton: document.getElementById("loadAccountsButton"),
    accountsTableBody: document.getElementById("accountsTableBody"),
    accountSelect: document.getElementById("accountSelect"),
    depositForm: document.getElementById("depositForm"),
    withdrawForm: document.getElementById("withdrawForm"),
    transferForm: document.getElementById("transferForm"),
    transactionFilterForm: document.getElementById("transactionFilterForm"),
    transactionTypeFilter: document.getElementById("transactionTypeFilter"),
    transactionAccountFilter: document.getElementById("transactionAccountFilter"),
    transactionsTableBody: document.getElementById("transactionsTableBody")
};

const state = {
    profile: null,
    accounts: [],
    allTransactions: [],
    filteredTransactions: []
};

const logger = createLogger(elements.logOutput);

function sleep(ms) {
    return new Promise((resolve) => setTimeout(resolve, ms));
}

function isUserNotSyncedError(error) {
    const message = error instanceof Error ? error.message : String(error || "");
    const normalized = message.toLowerCase();
    return normalized.includes("[404]") && normalized.includes("user with id") && normalized.includes("not found");
}

function groupTransactionsByAccount(transactions) {
    const grouped = new Map();
    (transactions || []).forEach((transaction) => {
        const accountId = transaction?.accountId;
        if (!accountId) {
            return;
        }
        const list = grouped.get(accountId) || [];
        list.push(transaction);
        grouped.set(accountId, list);
    });
    return grouped;
}

function enrichAccountsWithTransactions(accounts, allTransactions) {
    const grouped = groupTransactionsByAccount(allTransactions);
    return (accounts || []).map((account) => {
        const accountId = account?.accountId;
        const transactions = grouped.get(accountId) || [];
        return {
            ...account,
            transactions
        };
    });
}

function buildProfileView(profile, enrichedAccounts) {
    if (!profile) {
        return null;
    }
    return {
        ...profile,
        accounts: enrichedAccounts
    };
}

function setProfile(profileView) {
    elements.profileOutput.textContent = profileView
            ? JSON.stringify(profileView, null, 2)
            : "Профиль не загружен.";
}

function updateAccountSelectors(accounts) {
    const currentOperationAccount = elements.accountSelect.value;
    const currentFilterAccount = elements.transactionAccountFilter.value;

    elements.accountSelect.innerHTML = "<option value=''>Выбери счет</option>";
    elements.transactionAccountFilter.innerHTML = "<option value=''>Все счета</option>";

    (accounts || []).forEach((account) => {
        const operationOption = document.createElement("option");
        operationOption.value = account.accountId;
        operationOption.textContent = `${account.accountId} (${formatMoney(account.balance)})`;
        elements.accountSelect.appendChild(operationOption);

        const txOption = document.createElement("option");
        txOption.value = account.accountId;
        txOption.textContent = account.accountId;
        elements.transactionAccountFilter.appendChild(txOption);
    });

    if ((accounts || []).some((account) => account.accountId === currentOperationAccount)) {
        elements.accountSelect.value = currentOperationAccount;
    }
    if ((accounts || []).some((account) => account.accountId === currentFilterAccount)) {
        elements.transactionAccountFilter.value = currentFilterAccount;
    }
}

function renderAccounts(accounts) {
    if (!Array.isArray(accounts) || accounts.length === 0) {
        renderEmptyRow(elements.accountsTableBody, 3, "Счета отсутствуют");
        return;
    }

    elements.accountsTableBody.innerHTML = "";
    accounts.forEach((account) => {
        const transactionsCount = Array.isArray(account.transactions) ? account.transactions.length : 0;
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${account.accountId}</td>
            <td>${formatMoney(account.balance)}</td>
            <td>${transactionsCount}</td>
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

function updateStats(accounts, allTransactions) {
    const count = Array.isArray(accounts) ? accounts.length : 0;
    const total = (accounts || []).reduce((sum, account) => sum + Number(account.balance || 0), 0);
    const txCount = Array.isArray(allTransactions) ? allTransactions.length : 0;
    elements.accountCountValue.textContent = String(count);
    elements.totalBalanceValue.textContent = formatMoney(total);
    elements.transactionsCountValue.textContent = String(txCount);
}

function selectedAccountId() {
    const value = elements.accountSelect.value;
    if (!value) {
        throw new Error("Выбери счет перед выполнением операции");
    }
    return value;
}

function applyTransactionFilters() {
    const selectedType = String(elements.transactionTypeFilter.value || "").trim().toUpperCase();
    const selectedAccountId = String(elements.transactionAccountFilter.value || "").trim();

    state.filteredTransactions = (state.allTransactions || []).filter((transaction) => {
        if (selectedType && String(transaction.transactionType || "").toUpperCase() !== selectedType) {
            return false;
        }
        if (selectedAccountId && String(transaction.accountId || "") !== selectedAccountId) {
            return false;
        }
        return true;
    });

    renderTransactions(state.filteredTransactions);
}

function renderFromState() {
    const enrichedAccounts = enrichAccountsWithTransactions(state.accounts, state.allTransactions);
    const profileView = buildProfileView(state.profile, enrichedAccounts);

    setProfile(profileView);
    updateAccountSelectors(enrichedAccounts);
    renderAccounts(enrichedAccounts);
    updateStats(enrichedAccounts, state.allTransactions);
    applyTransactionFilters();
}

async function refreshAll() {
    const profile = await apiRequest("/users/me");
    const accounts = await apiRequest("/users/me/accounts");
    const transactions = await apiRequest("/users/me/transactions");

    state.profile = profile || null;
    state.accounts = Array.isArray(accounts) ? accounts : [];
    state.allTransactions = Array.isArray(transactions) ? transactions : [];

    renderFromState();
}

async function refreshAllWithSyncRetry(maxAttempts = 5) {
    for (let attempt = 1; attempt <= maxAttempts; attempt += 1) {
        try {
            await refreshAll();
            return;
        } catch (error) {
            if (!isUserNotSyncedError(error) || attempt === maxAttempts) {
                throw error;
            }
            logger.info(
                `Жду синхронизацию пользователя с bank (${attempt}/${maxAttempts}), повторяю загрузку...`
            );
            await sleep(300 * attempt);
        }
    }
}

async function createAccountWithRetry(maxAttempts = 5) {
    for (let attempt = 1; attempt <= maxAttempts; attempt += 1) {
        try {
            return await apiRequest("/users/me/accounts", {method: "POST"});
        } catch (error) {
            if (!isUserNotSyncedError(error) || attempt === maxAttempts) {
                throw error;
            }
            logger.info(
                `Пользователь еще синхронизируется с bank (${attempt}/${maxAttempts}), повторяю создание счёта...`
            );
            await sleep(300 * attempt);
        }
    }
    throw new Error("Не удалось создать счёт");
}

elements.refreshButton.addEventListener("click", async () => {
    try {
        await refreshAllWithSyncRetry();
        logger.info("Данные обновлены");
    } catch (error) {
        logger.error("Ошибка обновления", error.message);
    }
});

elements.logoutButton.addEventListener("click", async () => {
    await performLogout(logger);
    window.location.replace("/ui/login.html");
});

elements.addFriendButton.addEventListener("click", async () => {
    const friendId = String(new FormData(elements.friendForm).get("friendId") || "").trim();
    if (!isUuid(friendId)) {
        logger.error("Неверный friendId", "Ожидается UUID");
        return;
    }

    try {
        await apiRequest("/users/me/friends", {method: "POST", body: {friendId}});
        logger.info(`Друг добавлен: ${friendId}`);
        await refreshAllWithSyncRetry();
    } catch (error) {
        logger.error("Ошибка добавления друга", error.message);
    }
});

elements.removeFriendButton.addEventListener("click", async () => {
    const friendId = String(new FormData(elements.friendForm).get("friendId") || "").trim();
    if (!isUuid(friendId)) {
        logger.error("Неверный friendId", "Ожидается UUID");
        return;
    }

    try {
        await apiRequest("/users/me/friends", {method: "DELETE", body: {friendId}});
        logger.info(`Друг удален: ${friendId}`);
        await refreshAllWithSyncRetry();
    } catch (error) {
        logger.error("Ошибка удаления друга", error.message);
    }
});

elements.createAccountButton.addEventListener("click", async () => {
    try {
        const created = await createAccountWithRetry();
        logger.info("Счёт создан", created);
        if (created && created.accountId) {
            elements.accountSelect.value = created.accountId;
            elements.transactionAccountFilter.value = created.accountId;
        }
        await refreshAllWithSyncRetry();
    } catch (error) {
        logger.error("Ошибка создания счёта", error.message);
    }
});

elements.loadAccountsButton.addEventListener("click", async () => {
    try {
        await refreshAllWithSyncRetry();
        logger.info("Счета обновлены");
    } catch (error) {
        logger.error("Ошибка загрузки счетов", error.message);
    }
});

elements.depositForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    try {
        const accountId = selectedAccountId();
        const amount = Number(new FormData(elements.depositForm).get("amount"));
        if (!Number.isFinite(amount) || amount <= 0) {
            throw new Error("Сумма должна быть положительной");
        }

        await apiRequest(`/users/me/accounts/${accountId}/deposit`, {
            method: "POST",
            body: {amount}
        });
        logger.info(`Пополнение: ${formatMoney(amount)} -> ${accountId}`);
        await refreshAllWithSyncRetry();
    } catch (error) {
        logger.error("Ошибка пополнения", error.message);
    }
});

elements.withdrawForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    try {
        const accountId = selectedAccountId();
        const amount = Number(new FormData(elements.withdrawForm).get("amount"));
        if (!Number.isFinite(amount) || amount <= 0) {
            throw new Error("Сумма должна быть положительной");
        }

        await apiRequest(`/users/me/accounts/${accountId}/withdraw`, {
            method: "POST",
            body: {amount}
        });
        logger.info(`Списание: ${formatMoney(amount)} <- ${accountId}`);
        await refreshAllWithSyncRetry();
    } catch (error) {
        logger.error("Ошибка списания", error.message);
    }
});

elements.transferForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    const formData = new FormData(elements.transferForm);
    try {
        const fromAccountId = selectedAccountId();
        const toAccountId = String(formData.get("toAccountId") || "").trim();
        const amount = Number(formData.get("amount"));
        if (!isUuid(toAccountId)) {
            throw new Error("To Account ID должен быть UUID");
        }
        if (!Number.isFinite(amount) || amount <= 0) {
            throw new Error("Сумма должна быть положительной");
        }

        await apiRequest(`/users/me/accounts/${fromAccountId}/transfer`, {
            method: "POST",
            body: {toAccountId, amount}
        });
        logger.info(`Перевод: ${formatMoney(amount)} ${fromAccountId} -> ${toAccountId}`);
        await refreshAllWithSyncRetry();
    } catch (error) {
        logger.error("Ошибка перевода", error.message);
    }
});

elements.transactionFilterForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    try {
        applyTransactionFilters();
        logger.info("Фильтр транзакций применен");
    } catch (error) {
        logger.error("Ошибка фильтрации транзакций", error.message);
    }
});

const role = ensureRoleOrRedirect(["CLIENT"]);
if (!role) {
    throw new Error("Unauthorized");
}

setSessionBadge(elements.sessionBadge);
logger.info("Клиентская консоль готова");

refreshAllWithSyncRetry()
    .then(() => logger.info("Первичная загрузка выполнена"))
    .catch((error) => logger.error("Ошибка первичной загрузки", error.message));
