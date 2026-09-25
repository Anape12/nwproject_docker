document.querySelectorAll("[data-selected]").forEach((element) => {
    element.selected = element.dataset.selected === "true";
});

document.querySelectorAll("[data-checked]").forEach((element) => {
    element.checked = element.dataset.checked === "true";
});

document.querySelectorAll("[data-disabled]").forEach((element) => {
    element.disabled = element.dataset.disabled === "true";
});

document.querySelectorAll("[data-readonly]").forEach((element) => {
    element.readOnly = element.dataset.readonly === "true";
});

document.querySelectorAll("[data-required]").forEach((element) => {
    element.required = element.dataset.required === "true";
});
