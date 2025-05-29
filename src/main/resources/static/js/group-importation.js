async function importGroups(fileContent) {
    let responseData;
    await fetch("https://spring2025.nighthawkcodingsociety.com/api/groups/bulk/create", {
        method: "POST",
        body: fileContent,
        cache: "no-cache",
        headers: new Headers({
            "Content-Type": "application/json"
        })
    }).then((response) => response.json()).then((data) => {
        responseData = data;
    });
    return responseData;
}

document.getElementById("import-all").addEventListener("click", async () => {
    const input = document.getElementById("groupAllFileUpload"); // your input element for file
    if (input.files.length !== 1) {
        alert("You must upload a file.");
        return;
    }

    let file = input.files[0];
    let text = await file.text();

    try {
        const jsonData = JSON.parse(text);
        if (!Array.isArray(jsonData)) {
            alert("This import is expecting a JSON array.");
            return;
        }

        const result = await importGroups(text);
        alert("Groups imported successfully.");
        console.log(result);
        // Optionally reload or update UI:
        // location.reload();
    } catch (err) {
        alert("Invalid JSON format.");
        console.error("JSON parse error:", err);
    }
});
