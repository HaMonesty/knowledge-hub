"use strict";

const $ = selector => document.querySelector(selector);

const state = {
    token: sessionStorage.getItem("kh-token"),
    user: null,
    page: 1,
    total: 0,
    categories: [],
    editing: null,
    categoryId: null,
    register: false,
    view: "knowledge",
    listVersion: 0
};

// 显示操作结果。
function message(text, error = false) {
    $("#notice").textContent = text;
    $("#notice").className = error ? "error" : "";
}

// 更新登录信息和按钮权限。
function updateAccount() {
    const admin = state.user?.role === "ADMIN";

    $("#account").textContent = state.user
        ? `${state.user.nickname} · ${state.user.role}`
        : "未登录";

    $("#logout").hidden = !state.user;
    $("#auth-open").hidden = !!state.user;
    $("#category-new").disabled = !admin;
    $("#new").disabled = !state.user;
}

// 清除当前标签页的登录信息。
function clearSession() {
    state.token = null;
    state.user = null;
    state.total = 0;
    state.page = 1;
    state.listVersion++;

    sessionStorage.removeItem("kh-token");

    $("#cards").replaceChildren(
        textNode("p", "登录后查看自己的知识。", "empty")
    );
    $("#category-list").replaceChildren();
    $("#total").textContent = "共 0 条";
    $("#page").textContent = "第 1 页";
    $("#prev").disabled = true;
    $("#next").disabled = true;
    $("#response").textContent = "尚未请求";

    for (const id of ["editor", "category-dialog"]) {
        if ($("#" + id).open) {
            $("#" + id).close();
        }
    }

    updateAccount();
}

// 统一请求接口；Nginx将/api/转发到Java后端。
async function api(path, method = "GET", body) {
    const headers = {};

    if (state.token && !path.startsWith("/auth/")) {
        headers.Authorization = `Bearer ${state.token}`;
    }

    if (body !== undefined) {
        headers["Content-Type"] = "application/json";
    }

    const response = await fetch("/api/v1" + path, {
        method,
        headers,
        body: body === undefined ? undefined : JSON.stringify(body)
    });

    const raw = await response.text();
    let data = null;

    if (raw) {
        try {
            data = JSON.parse(raw);
        } catch {
            // Nginx错误页面等响应不一定是JSON。
        }
    }

    const display = path === "/auth/login" && response.ok
        ? { ...data, accessToken: "[隐藏]" }
        : data;

    $("#response").textContent =
        `${method} ${path}\nHTTP ${response.status}\n` +
        (data === null ? raw : JSON.stringify(display, null, 2));

    if (!response.ok) {
        if (response.status === 401 && !path.startsWith("/auth/")) {
            clearSession();
        }

        throw new Error(
            data?.message ||
            `请求失败（${response.status}），请检查后端与Nginx配置`
        );
    }

    if (response.status !== 204 && data === null) {
        throw new Error("接口未返回JSON，请检查Nginx代理配置");
    }

    return data;
}

// 捕获交互过程中的异常并显示。
async function run(action) {
    try {
        await action();
    } catch (error) {
        message(error.message, true);
    }
}

// 使用textContent显示后端内容，避免将用户内容作为HTML执行。
function textNode(tag, text, className) {
    const node = document.createElement(tag);
    node.textContent = text ?? "";

    if (className) {
        node.className = className;
    }

    return node;
}

function actionButton(label, action) {
    const button = document.createElement("button");
    button.type = "button";
    button.textContent = label;
    button.onclick = () => run(action);
    return button;
}

// 查询分类，用于列表和草稿分类选择。
async function loadCategories() {
    state.categories = await api("/categories");
}

// 查询当前用户的知识分页。
async function loadKnowledge() {
    if (!state.user) {
        return;
    }

    const version = ++state.listVersion;
    const size = Number($("#size").value);

    const params = new URLSearchParams({
        page: state.page,
        size
    });

    if ($("#status").value) {
        params.set("status", $("#status").value);
    }

    const data = await api("/knowledge/mine?" + params);

    // 防止较早发出的请求覆盖更新的查询结果。
    if (version !== state.listVersion) {
        return;
    }

    state.total = data.total;
    $("#cards").replaceChildren();

    for (const knowledge of data.records) {
        const card = textNode("article", "", "card");

        card.append(
            textNode(
                "span",
                knowledge.status === "DRAFT" ? "草稿" : "已发布",
                "badge"
            ),
            textNode("h3", knowledge.title),
            textNode("p", knowledge.summary || "暂无摘要"),
            textNode(
                "div",
                `#${knowledge.id} · ${knowledge.updatedAt || ""}`,
                "meta"
            )
        );

        const actions = textNode("div", "", "actions");

        if (knowledge.status === "DRAFT") {
            actions.append(
                actionButton("查看", () =>
                    openEditor(knowledge.id, true)
                ),
                actionButton("编辑", () =>
                    openEditor(knowledge.id, false)
                ),
                actionButton("删除", async () => {
                    if (!confirm(
                        `永久删除「${knowledge.title}」？此操作无法撤销。`
                    )) {
                        return;
                    }

                    await api("/knowledge/" + knowledge.id, "DELETE");

                    if (data.records.length === 1 && state.page > 1) {
                        state.page--;
                    }

                    await loadKnowledge();
                    message("草稿已删除");
                })
            );
        } else {
            const pending = actionButton("已发布详情 · 待接入", () => {});
            pending.disabled = true;
            actions.append(pending);
        }

        card.append(actions);
        $("#cards").append(card);
    }

    if (!data.records.length) {
        $("#cards").append(
            textNode("p", "当前条件下暂无知识。", "empty")
        );
    }

    $("#total").textContent = `共 ${data.total} 条`;
    $("#page").textContent = `第 ${state.page} 页`;
    $("#prev").disabled = state.page === 1;
    $("#next").disabled = state.page * size >= data.total;
}

// 显示分类列表，仅管理员显示维护按钮。
async function renderCategories() {
    await loadCategories();
    $("#category-list").replaceChildren();

    for (const category of state.categories) {
        const row = textNode("div", "", "category-row");
        row.append(
            textNode("span", `#${category.id}　${category.name}`)
        );

        if (state.user?.role === "ADMIN") {
            row.append(
                actionButton("修改", () => {
                    state.categoryId = category.id;
                    $("#category-form").elements.name.value = category.name;
                    $("#category-dialog").showModal();
                }),
                actionButton("删除", async () => {
                    if (!confirm(`删除分类「${category.name}」？`)) {
                        return;
                    }

                    await api("/categories/" + category.id, "DELETE");
                    await renderCategories();
                    message("分类已删除");
                })
            );
        }

        $("#category-list").append(row);
    }

    if (!state.categories.length) {
        $("#category-list").append(
            textNode("p", "暂无分类。", "empty")
        );
    }
}

// 打开草稿创建、编辑或只读详情窗口。
async function openEditor(id = null, readOnly = false) {
    await loadCategories();

    const form = $("#edit-form");
    form.reset();

    const select = form.elements.categoryId;
    select.replaceChildren(new Option("不选择分类", ""));

    for (const category of state.categories) {
        select.add(new Option(category.name, category.id));
    }

    state.editing = id;

    if (id !== null) {
        const knowledge = await api("/knowledge/" + id);

        for (const key of ["title", "summary", "content", "categoryId"]) {
            form.elements[key].value = knowledge[key] ?? "";
        }
    }

    for (const element of form.elements) {
        if (["INPUT", "TEXTAREA", "SELECT"].includes(element.tagName)) {
            element.disabled = readOnly;
        }
    }

    $("#save").hidden = readOnly;
    $("#editor-title").textContent = readOnly
        ? "草稿详情"
        : id !== null ? "编辑草稿" : "创建草稿";

    $("#editor").showModal();
}

// 刷新当前页面数据。
async function refresh() {
    if (state.view === "roadmap") {
        return;
    }

    if (!state.user) {
        message("请先登录");
        return;
    }

    if (state.view === "knowledge") {
        await loadKnowledge();
    } else if (state.view === "categories") {
        await renderCategories();
    }
}

// 切换导航页面。
for (const button of document.querySelectorAll("[data-view]")) {
    button.onclick = () => run(async () => {
        state.view = button.dataset.view;

        for (const name of ["knowledge", "categories", "roadmap"]) {
            $("#" + name).hidden = name !== state.view;
        }

        for (const nav of document.querySelectorAll("[data-view]")) {
            nav.classList.toggle("active", nav === button);
        }

        const titles = {
            knowledge: "我的知识",
            categories: "分类管理",
            roadmap: "更多功能"
        };

        $("#heading").textContent = titles[state.view];
        await refresh();
    });
}

// 后续功能只展示入口，不调用尚未确定的接口。
const futureFeatures = [
    "知识发布与撤回",
    "标签管理",
    "收藏与浏览记录",
    "热门知识排行",
    "图片与附件上传 / OSS",
    "AI 摘要",
    "AI 标签推荐",
    "AI 代码解释",
    "全文搜索 / Elasticsearch",
    "RAG 知识问答",
    "个人信息与密码修改",
    "管理员用户管理"
];

for (const name of futureFeatures) {
    const card = textNode("article", "", "card");

    card.append(
        textNode("span", "规划中", "badge"),
        textNode("h3", name),
        textNode("p", "接口确定并完成后接入。")
    );

    const button = actionButton("待开发", () => {});
    button.disabled = true;
    card.append(button);
    $("#future").append(card);
}

// 登录、注册与退出。
$("#auth-open").onclick = () => $("#auth").showModal();

$("#logout").onclick = () => {
    clearSession();
    message("已退出当前浏览器登录；服务端令牌撤销尚未实现");
};

$("#auth-switch").onclick = () => {
    state.register = !state.register;
    $("#nickname-field").hidden = !state.register;
    $("#auth-title").textContent = state.register
        ? "注册 KnowledgeHub"
        : "登录 KnowledgeHub";
    $("#auth-switch").textContent = state.register
        ? "切换登录"
        : "切换注册";
};

$("#auth-form").onsubmit = event => {
    event.preventDefault();

    run(async () => {
        const form = $("#auth-form");
        const submit = form.querySelector('button[type="submit"]');
        submit.disabled = true;

        try {
            const body = {
                username: form.elements.username.value,
                password: form.elements.password.value
            };

            if (state.register) {
                if (!/[A-Za-z]/.test(body.password) ||
                    !/[0-9]/.test(body.password)) {
                    throw new Error("注册密码需同时包含字母和数字");
                }

                if (form.elements.nickname.value) {
                    body.nickname = form.elements.nickname.value;
                }

                await api("/auth/register", "POST", body);
                $("#auth-switch").click();
                form.elements.password.value = "";
                message("注册成功，请登录");
                return;
            }

            const result = await api("/auth/login", "POST", body);
            state.token = result.accessToken;
            sessionStorage.setItem("kh-token", state.token);

            try {
                state.user = await api("/users/me");
            } catch (error) {
                clearSession();
                throw error;
            }

            updateAccount();
            $("#auth").close();
            form.elements.password.value = "";
            state.page = 1;
            await refresh();
            message("登录成功");
        } finally {
            submit.disabled = false;
        }
    });
};

// 列表筛选及翻页。
$("#new").onclick = () => run(() => openEditor());
$("#refresh").onclick = () => run(refresh);

for (const id of ["status", "size"]) {
    $("#" + id).onchange = () => run(async () => {
        state.page = 1;
        await loadKnowledge();
    });
}

$("#prev").onclick = () => run(async () => {
    if (state.user && state.page > 1) {
        state.page--;
        await loadKnowledge();
    }
});

$("#next").onclick = () => run(async () => {
    if (state.user &&
        state.page * Number($("#size").value) < state.total) {
        state.page++;
        await loadKnowledge();
    }
});

// 保存草稿。
$("#edit-form").onsubmit = event => {
    event.preventDefault();

    run(async () => {
        const form = $("#edit-form");

        const body = {
            title: form.elements.title.value,
            summary: form.elements.summary.value || null,
            content: form.elements.content.value || null,
            categoryId: form.elements.categoryId.value
                ? Number(form.elements.categoryId.value)
                : null
        };

        $("#save").disabled = true;

        try {
            const editing = state.editing !== null;

            await api(
                "/knowledge" + (editing ? "/" + state.editing : ""),
                editing ? "PUT" : "POST",
                body
            );

            $("#editor").close();
            state.page = 1;
            await loadKnowledge();
            message("草稿已保存");
        } finally {
            $("#save").disabled = false;
        }
    });
};

// 新增或修改分类。
$("#category-new").onclick = () => {
    state.categoryId = null;
    $("#category-form").reset();
    $("#category-dialog").showModal();
};

$("#category-form").onsubmit = event => {
    event.preventDefault();

    run(async () => {
        const form = $("#category-form");
        const submit = form.querySelector('button[type="submit"]');
        submit.disabled = true;

        try {
            const editing = state.categoryId !== null;

            await api(
                "/categories" + (editing ? "/" + state.categoryId : ""),
                editing ? "PUT" : "POST",
                { name: form.elements.name.value }
            );

            $("#category-dialog").close();
            await renderCategories();
            message("分类已保存");
        } finally {
            submit.disabled = false;
        }
    });
};

// 关闭弹窗。
for (const button of document.querySelectorAll("[data-close]")) {
    button.onclick = () => $("#" + button.dataset.close).close();
}

// 页面刷新后恢复当前标签页的登录状态。
updateAccount();
$("#prev").disabled = true;
$("#next").disabled = true;

if (state.token) {
    run(async () => {
        state.user = await api("/users/me");
        updateAccount();
        await refresh();
    });
}