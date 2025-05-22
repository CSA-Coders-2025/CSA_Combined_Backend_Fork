async function importPeople(file) {
    let data1;
    await fetch("/mvc/import/all/person", {
        body: file,
        method: "POST",
        cache: "no-cache",
        headers: new Headers({
            "content-type": "application/json"
        })
    }).then((response) => response.json()).then((data) => {
        data1 = data;
    })
    return data1;
}

document.getElementById("import-all").addEventListener("click", async () => {
    let content = await importPeople(document.getElementById("personAllFileUpload").files[0]);
    console.log(content);
})