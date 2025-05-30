async function importGroups(fileContent) {
    try {
        const response = await fetch("/api/groups/bulk/create", {
            method: "POST",
            body: fileContent,
            cache: "no-cache",
            headers: new Headers({
                "content-type": "application/json"
            })
        });

        if (!response.ok) {
            // Try to parse error details
            let errorBody;
            try {
                errorBody = await response.text();
            } catch(e) {
                errorBody = "<no body>";
            }
            console.error("Server responded with error:", response.status, errorBody);
            throw new Error(`Server error: ${response.status} - ${errorBody}`);
        }

        const data = await response.json();
        return data;
    } catch (error) {
        console.error("Import error:", error);
        alert("An error occurred during import.");
        return null;
    }
}

document.getElementById("import-all").addEventListener("click", async () => {
    const input = document.getElementById("groupAllFileUpload");
    if(input.files.length != 1){
        alert("You must upload a file.");
        return;
    }
    let file = input.files[0];
    let text = await file.text();

    console.log("File content loaded:", text);

    try {
        const json = JSON.parse(text);
        console.log("Parsed JSON:", json);
        if(!Array.isArray(json)){
            alert("This import is expecting an array.");
            return;
        }
        let content = await importGroups(text);
        console.log("Import result:", content);
        alert("Groups imported successfully.");
    } catch (e) {
        console.error("Invalid JSON:", e);
        alert("Invalid JSON file.");
    }
});
