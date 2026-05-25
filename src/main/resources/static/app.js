/**
 * Task Management API Tester - app.js
 */

const state = {
    token: localStorage.getItem("accessToken") || null,
    refreshToken: localStorage.getItem("refreshToken") || null,
    user: JSON.parse(localStorage.getItem("user")) || null,
    projects: [],
    tasks: [],
    selectedProjectId: null,
};

// --- DOM ELEMENTS ---
const elements = {
    sessionStatus: document.getElementById("sessionStatus"),
    logoutBtn: document.getElementById("logoutBtn"),
    loginForm: document.getElementById("loginForm"),
    registerForm: document.getElementById("registerForm"),
    authTabs: document.querySelectorAll("[data-auth-tab]"),
    togglePasswords: document.querySelectorAll(".toggle-password"),
    tokenInput: document.getElementById("tokenInput"),
    saveTokenBtn: document.getElementById("saveTokenBtn"),
    refreshBtn: document.getElementById("refreshBtn"),
    
    projectForm: document.getElementById("projectForm"),
    projectsList: document.getElementById("projectsList"),
    loadProjectsBtn: document.getElementById("loadProjectsBtn"),
    clearProjectBtn: document.getElementById("clearProjectBtn"),
    
    taskForm: document.getElementById("taskForm"),
    tasksList: document.getElementById("tasksList"),
    loadTasksBtn: document.getElementById("loadTasksBtn"),
    clearTaskBtn: document.getElementById("clearTaskBtn"),
    selectedProjectLabel: document.getElementById("selectedProjectLabel"),
    
    resetPasswordForm: document.getElementById("resetPasswordForm"),
    selfResetPasswordForm: document.getElementById("selfResetPasswordForm"),
    
    responseLog: document.getElementById("responseLog"),
    clearLogBtn: document.getElementById("clearLogBtn"),
    toastContainer: document.getElementById("toastContainer"),
    
    projectSidebar: document.getElementById("projectSidebar"),
    sidebarContent: document.getElementById("projectSidebarContent"),
    closeSidebarBtn: document.getElementById("closeSidebarBtn"),
    sidebarEditProjectBtn: document.getElementById("sidebarEditProjectBtn"),
    sidebarDeleteProjectBtn: document.getElementById("sidebarDeleteProjectBtn"),
};

// --- API UTILITY ---
async function api(path, options = {}) {
    const url = path.startsWith("http") ? path : `/api${path}`;
    const headers = {
        "Content-Type": "application/json",
        ...options.headers
    };
    
    if (state.token) {
        headers["Authorization"] = `Bearer ${state.token}`;
    }
    
    try {
        const response = await fetch(url, { ...options, headers });
        const data = response.status === 204 ? null : await response.json();
        
        logResponse(path, options.method || "GET", response.status, data);
        
        if (!response.ok) {
            throw { status: response.status, ...data };
        }
        
        return data;
    } catch (error) {
        throw error;
    }
}

function logResponse(path, method, status, data) {
    const timestamp = new Date().toLocaleTimeString();
    const entry = `[${timestamp}] ${method} ${path} -> ${status}\n${data ? JSON.stringify(data, null, 2) : "(No body)"}\n\n`;
    elements.responseLog.textContent = entry + elements.responseLog.textContent;
}

function toast(title, message, type = "success") {
    const t = document.createElement("div");
    t.className = `toast ${type}`;
    t.innerHTML = `<strong>${title}</strong><p>${message}</p>`;
    elements.toastContainer.appendChild(t);
    setTimeout(() => t.remove(), 5000);
}

// --- AUTH LOGIC ---
function updateSessionUI() {
    const isLoggedIn = !!state.user;
    
    if (isLoggedIn) {
        elements.sessionStatus.innerHTML = `Signed in as <strong>${state.user.name}</strong> (Code: <code>${state.user.userCode}</code>)`;
        elements.logoutBtn.classList.remove("hidden");
        elements.tokenInput.value = state.token;
        
        // Show/hide panels
        document.querySelector(".profile-panel").classList.remove("hidden");
        document.querySelector(".project-panel").classList.remove("hidden");
        document.querySelector(".tasks-panel").classList.remove("hidden");
        
        const isAdmin = state.user.role === "ADMIN";
        document.querySelector(".admin-panel").classList.toggle("hidden", !isAdmin);
    } else {
        elements.sessionStatus.textContent = "Not signed in";
        elements.logoutBtn.classList.add("hidden");
        elements.tokenInput.value = "";
        
        // Hide panels
        document.querySelector(".profile-panel").classList.add("hidden");
        document.querySelector(".project-panel").classList.add("hidden");
        document.querySelector(".tasks-panel").classList.add("hidden");
        document.querySelector(".admin-panel").classList.add("hidden");
    }
}

async function handleLogin(e) {
    e.preventDefault();
    const formData = new FormData(e.target);
    const body = Object.fromEntries(formData.entries());
    
    try {
        const data = await api("/auth/login", { method: "POST", body: JSON.stringify(body) });
        saveSession(data);
        toast("Logged in", `Welcome back, ${data.user.name}`);
        sync();
    } catch (err) {
        toast("Login Failed", err.message || "Check console", "error");
    }
}

async function handleRegister(e) {
    e.preventDefault();
    const formData = new FormData(e.target);
    const body = Object.fromEntries(formData.entries());
    
    if (body.password.length < 8) {
        return toast("Validation Error", "Password must be at least 8 chars", "error");
    }
    
    try {
        const data = await api("/auth/register", { method: "POST", body: JSON.stringify(body) });
        saveSession(data);
        toast("Registered", `Account created for ${data.user.name}`);
        sync();
    } catch (err) {
        toast("Registration Failed", err.message || "Check console", "error");
    }
}

function saveSession(data) {
    state.token = data.accessToken;
    state.refreshToken = data.refreshToken;
    state.user = data.user;
    localStorage.setItem("accessToken", data.accessToken);
    localStorage.setItem("refreshToken", data.refreshToken);
    localStorage.setItem("user", JSON.stringify(data.user));
    updateSessionUI();
}

function logout() {
    state.token = null;
    state.refreshToken = null;
    state.user = null;
    localStorage.clear();
    updateSessionUI();
    elements.projectsList.innerHTML = "<div class=\"empty\">Signed out</div>";
    elements.tasksList.innerHTML = "<div class=\"empty\">Signed out</div>";
    toast("Logged out", "Session cleared");
}

async function refreshSession() {
    if (!state.refreshToken) return toast("No Token", "No refresh token available", "warning");
    
    try {
        const data = await api("/auth/refresh", { 
            method: "POST", 
            body: JSON.stringify({ refreshToken: state.refreshToken }) 
        });
        saveSession(data);
        toast("Refreshed", "New access token acquired");
    } catch (err) {
        toast("Refresh Failed", err.message, "error");
    }
}

// --- PROJECT LOGIC ---
async function loadProjects() {
    if (!state.token) return;
    try {
        const projects = await api("/projects");
        state.projects = projects;
        renderProjects();
    } catch (err) {
        toast("Error", "Could not load projects", "error");
    }
}

function renderProjects() {
    if (state.projects.length === 0) {
        elements.projectsList.innerHTML = "<div class=\"empty\">No projects found</div>";
        return;
    }
    
    elements.projectsList.innerHTML = state.projects.map(p => {
        const isAdmin = p.admins.some(a => a.id === state.user?.id) || p.owner.id === state.user?.id;
        return `
            <div class="item project-item ${state.selectedProjectId === p.id ? "active" : ""}" data-id="${p.id}">
                <div class="item-head">
                    <span class="item-title">${p.name}</span>
                    <span class="badge">${isAdmin ? "ADMIN" : "MEMBER"}</span>
                </div>
                <div class="meta">
                    <span>Owned by ${p.owner.name}</span>
                    <span>•</span>
                    <span>${p.members?.length || 0} members</span>
                </div>
                <div class="inline-actions">
                    <button class="secondary mini-btn view-btn" type="button">Details</button>
                    <button class="secondary mini-btn select-btn" type="button">Select</button>
                </div>
            </div>
        `;
    }).join("");
}

async function handleProjectSubmit(e) {
    e.preventDefault();
    const formData = new FormData(e.target);
    
    const membersRaw = formData.get("memberUserCodes") || "";
    const memberUserCodes = membersRaw.split(",").map(s => s.trim()).filter(s => s && !s.includes("@"));
    const memberEmails = membersRaw.split(",").map(s => s.trim()).filter(s => s && s.includes("@"));
    
    const body = {
        name: formData.get("name"),
        description: formData.get("description"),
        memberUserCodes: memberUserCodes,
        memberEmails: memberEmails
    };
    
    const id = formData.get("id");
    const method = id ? "PUT" : "POST";
    const path = id ? `/projects/${id}` : "/projects";
    
    try {
        await api(path, { method, body: JSON.stringify(body) });
        toast("Success", id ? "Project updated" : "Project created (You are Admin)");
        clearProjectForm();
        loadProjects();
    } catch (err) {
        toast("Save Failed", err.message, "error");
    }
}

function selectProject(id) {
    state.selectedProjectId = Number(id);
    const project = state.projects.find(p => p.id === state.selectedProjectId);
    elements.selectedProjectLabel.textContent = project ? `Active: ${project.name}` : "Select a project";
    renderProjects();
    loadTasks();
}

async function viewProjectDetails(id) {
    try {
        const project = await api(`/projects/${id}`);
        state.selectedProjectId = project.id;
        renderSidebar(project);
        elements.projectSidebar.classList.remove("hidden");
    } catch (err) {
        toast("Error", "Could not load project details", "error");
    }
}

function renderSidebar(p) {
    elements.sidebarContent.innerHTML = `
        <div class="sidebar-section">
            <h3>${p.name}</h3>
            <p>${p.description || "No description"}</p>
        </div>
        <div class="sidebar-section">
            <h4>Ownership & Admins</h4>
            <div class="member-chips">
                <div class="member-chip">
                    <strong>${p.owner.name}</strong> (Owner)<br>
                    <small>${p.owner.email}</small>
                </div>
                ${p.admins.filter(a => a.id !== p.owner.id).map(a => `
                    <div class="member-chip">
                        <strong>${a.name}</strong> (Admin)<br>
                        <small>${a.userCode}</small>
                    </div>
                `).join("")}
            </div>
        </div>
        <div class="sidebar-section">
            <h4>Members</h4>
            <div class="member-chips">
                ${p.members.length > 0 
                    ? p.members.map(m => `
                        <div class="member-chip">
                            <strong>${m.name}</strong><br>
                            <small>${m.userCode}</small>
                        </div>
                    `).join("")
                    : "<p class=\"muted\">No members yet</p>"}
            </div>
        </div>
    `;
    
    const isAdmin = p.admins.some(a => a.id === state.user?.id) || p.owner.id === state.user?.id;
    
    elements.sidebarEditProjectBtn.classList.toggle("hidden", !isAdmin);
    elements.sidebarDeleteProjectBtn.classList.toggle("hidden", !isAdmin);

    elements.sidebarEditProjectBtn.onclick = () => {
        fillProjectForm(p);
        elements.projectSidebar.classList.add("hidden");
    };
    
    elements.sidebarDeleteProjectBtn.onclick = async () => {
        if (!confirm("Delete this project?")) return;
        try {
            await api(`/projects/${p.id}`, { method: "DELETE" });
            toast("Deleted", "Project removed");
            elements.projectSidebar.classList.add("hidden");
            if (state.selectedProjectId === p.id) {
                state.selectedProjectId = null;
                elements.selectedProjectLabel.textContent = "Select a project";
                elements.tasksList.innerHTML = "<div class=\"empty\">Select a project</div>";
            }
            loadProjects();
        } catch (err) {
            toast("Delete Failed", err.message, "error");
        }
    };
}

function fillProjectForm(p) {
    elements.projectForm.id.value = p.id;
    elements.projectForm.name.value = p.name;
    elements.projectForm.description.value = p.description || "";
    // Combine members and codes for the input
    const members = p.members.map(m => m.userCode).join(", ");
    elements.projectForm.memberUserCodes.value = members;
}

function clearProjectForm() {
    elements.projectForm.reset();
    elements.projectForm.id.value = "";
}

// --- TASK LOGIC ---
async function loadTasks() {
    if (!state.selectedProjectId) return;
    try {
        const tasks = await api(`/projects/${state.selectedProjectId}/tasks`);
        state.tasks = tasks;
        renderTasks();
    } catch (err) {
        toast("Error", "Could not load tasks", "error");
    }
}

function renderTasks() {
    if (!state.selectedProjectId) {
        elements.tasksList.innerHTML = "<div class=\"empty\">Select a project first</div>";
        return;
    }
    if (state.tasks.length === 0) {
        elements.tasksList.innerHTML = "<div class=\"empty\">No tasks in this project</div>";
        return;
    }
    
    elements.tasksList.innerHTML = state.tasks.map(t => `
        <div class="item task-item" data-id="${t.id}">
            <div class="item-head">
                <span class="item-title">${t.title}</span>
                <span class="badge ${t.status === "DONE" ? "" : "warning"}">${t.status}</span>
            </div>
            <p style="margin:0; font-size:13px;">${t.description || "No description"}</p>
            <div class="meta">
                <span>Assigned to: ${t.assignees.length > 0 ? t.assignees.map(a => a.name).join(", ") : "Unassigned"}</span>
                <span>•</span>
                <span>Due: ${t.dueDate ? new Date(t.dueDate).toLocaleString() : "N/A"}</span>
            </div>
            <div class="inline-actions">
                <button class="secondary mini-btn edit-task-btn" type="button">Edit</button>
                <button class="secondary mini-btn danger delete-task-btn" type="button">Delete</button>
            </div>
        </div>
    `).join("");
}

async function handleTaskSubmit(e) {
    e.preventDefault();
    if (!state.selectedProjectId) return toast("No Project", "Select a project first", "warning");
    
    const formData = new FormData(e.target);
    const rawAssignees = formData.get("assignedToUserCode") || "";
    const assignedToUserCodes = rawAssignees.split(",").map(s => s.trim()).filter(s => s && !s.includes("@"));
    const assignedToEmails = rawAssignees.split(",").map(s => s.trim()).filter(s => s && s.includes("@"));
    
    const body = {
        title: formData.get("title"),
        description: formData.get("description"),
        status: formData.get("status"),
        dueDate: formData.get("dueDate") || null,
        assignedToUserCodes: assignedToUserCodes,
        assignedToEmails: assignedToEmails
    };
    
    const id = formData.get("id");
    const method = id ? "PUT" : "POST";
    const path = id ? `/tasks/${id}` : `/projects/${state.selectedProjectId}/tasks`;
    
    try {
        await api(path, { method, body: JSON.stringify(body) });
        toast("Success", id ? "Task updated" : "Task created");
        clearTaskForm();
        loadTasks();
    } catch (err) {
        toast("Save Failed", err.message, "error");
    }
}

function fillTaskForm(t) {
    elements.taskForm.id.value = t.id;
    elements.taskForm.title.value = t.title;
    elements.taskForm.status.value = t.status;
    elements.taskForm.description.value = t.description || "";
    // Combine assignees codes and emails
    elements.taskForm.assignedToUserCode.value = t.assignees.map(a => a.userCode).join(", ");
    if (t.dueDate) {
        elements.taskForm.dueDate.value = t.dueDate.substring(0, 16);
    }
}

function clearTaskForm() {
    elements.taskForm.reset();
    elements.taskForm.id.value = "";
}

async function deleteTask(id) {
    if (!confirm("Delete this task?")) return;
    try {
        await api(`/tasks/${id}`, { method: "DELETE" });
        toast("Deleted", "Task removed");
        loadTasks();
    } catch (err) {
        toast("Delete Failed", err.message, "error");
    }
}

// --- ADMIN LOGIC ---
async function handleResetPassword(e) {
    e.preventDefault();
    const formData = new FormData(e.target);
    const targetEmail = formData.get("userId"); // This is the email in our updated backend
    const body = { newPassword: formData.get("newPassword") };
    
    try {
        // If targetEmail is the current user's email, we could use /users/reset-password
        // but for simplicity and to follow the user request, admins use this for anyone.
        await api(`/admin/users/${targetEmail}/reset-password`, { 
            method: "POST", 
            body: JSON.stringify(body) 
        });
        toast("Admin Action", "Password reset successfully for " + targetEmail);
        e.target.reset();
    } catch (err) {
        toast("Admin Error", err.message || "Forbidden", "error");
    }
}

async function handleSelfResetPassword(e) {
    e.preventDefault();
    const formData = new FormData(e.target);
    const body = { newPassword: formData.get("newPassword") };
    
    try {
        await api("/users/reset-password", { 
            method: "POST", 
            body: JSON.stringify(body) 
        });
        toast("Success", "Your password has been changed");
        e.target.reset();
    } catch (err) {
        toast("Error", err.message || "Could not change password", "error");
    }
}

// --- INITIALIZATION & SYNC ---
function sync() {
    updateSessionUI();
    if (state.token) {
        loadProjects();
    }
}

// Event Listeners
elements.loginForm.onsubmit = handleLogin;
elements.registerForm.onsubmit = handleRegister;
elements.logoutBtn.onclick = logout;
elements.refreshBtn.onclick = refreshSession;

elements.saveTokenBtn.onclick = () => {
    state.token = elements.tokenInput.value;
    localStorage.setItem("accessToken", state.token);
    toast("Saved", "Token updated manually");
    sync();
};

elements.authTabs.forEach(btn => {
    btn.onclick = () => {
        const tab = btn.dataset.authTab;
        elements.authTabs.forEach(b => b.classList.remove("active"));
        btn.classList.add("active");
        if (tab === "login") {
            elements.loginForm.classList.remove("hidden");
            elements.registerForm.classList.add("hidden");
        } else {
            elements.loginForm.classList.add("hidden");
            elements.registerForm.classList.remove("hidden");
        }
    };
});

elements.togglePasswords.forEach(btn => {
    btn.onclick = () => {
        const input = btn.previousElementSibling;
        const type = input.type === "password" ? "text" : "password";
        input.type = type;
        btn.textContent = type === "password" ? "Show" : "Hide";
    };
});

elements.projectForm.onsubmit = handleProjectSubmit;
elements.loadProjectsBtn.onclick = loadProjects;
elements.clearProjectBtn.onclick = clearProjectForm;

elements.projectsList.onclick = (e) => {
    const item = e.target.closest(".project-item");
    if (!item) return;
    const id = item.dataset.id;
    if (e.target.classList.contains("view-btn")) {
        viewProjectDetails(id);
    } else {
        selectProject(id);
    }
};

elements.taskForm.onsubmit = handleTaskSubmit;
elements.loadTasksBtn.onclick = loadTasks;
elements.clearTaskBtn.onclick = clearTaskForm;

elements.tasksList.onclick = (e) => {
    const item = e.target.closest(".task-item");
    if (!item) return;
    const id = item.dataset.id;
    if (e.target.classList.contains("edit-task-btn")) {
        const task = state.tasks.find(t => t.id === Number(id));
        fillTaskForm(task);
    } else if (e.target.classList.contains("delete-task-btn")) {
        deleteTask(id);
    }
};

elements.resetPasswordForm.onsubmit = handleResetPassword;
elements.selfResetPasswordForm.onsubmit = handleSelfResetPassword;
elements.clearLogBtn.onclick = () => elements.responseLog.textContent = "Ready.";
elements.closeSidebarBtn.onclick = () => elements.projectSidebar.classList.add("hidden");

// Initialize
sync();
