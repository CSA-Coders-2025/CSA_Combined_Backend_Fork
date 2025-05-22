import BlobBuilder from "./extraction.js";

async function getAllPeople() {
    let data1;
    await fetch("/mvc/extract/all/person", {
        method: "GET",
        cache: "no-cache",
    }).then((response) => response.json()).then((data) => {
        data1 = data;
    })
    return data1;
}

document.getElementById("export-all").addEventListener("click", async () => {
    let content = await getAllPeople();
    const blob = new BlobBuilder(BlobBuilder.fileTypeEnum.json, content);
    blob.downloadBlob("persons");
})

async function getAllPeopleInRanges() {
    let data1;
    await fetch("/mvc/extract/all/person", {
        method: "GET",
        cache: "no-cache",
    }).then((response) => response.json()).then((data) => {
        data1 = data;
    })
    return data1;
}

document.getElementById("export-ranges").addEventListener("click", async () => {
    let content = await getAllPeople();
    const blob = new BlobBuilder(BlobBuilder.fileTypeEnum.json, content);
    blob.downloadBlob("persons");
})


document.getElementById("exportTableAddRow").addEventListener("click", () => {
    const row = document.createElement("tr");
    const detail1 = document.createElement("td");
    detail1.innerHTML = `
        <div class="input-group mb-3">
            <input type="number" min="0" class="form-control">
            <span class="input-group-text">to</span>
            <input type="number"  min="0" class="form-control">
        </div>
    `;
    const detail2 = document.createElement("td");
    const button = document.createElement("button");
    button.setAttribute("class","btn btn-primary btn-sm");
    button.innerText = "-";
    button.addEventListener("click",()=>{
        row.remove();
    })

    detail2.appendChild(button);
    
    row.appendChild(detail1);
    row.appendChild(detail2);

    document.getElementById("exportTableBody").appendChild(row);

})