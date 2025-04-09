document.querySelector(".navbar").style.zIndex = 4
let isSidebarClosed = true
let leftSidebar = document.getElementById("ManagementMenu")
function toggleLeftSidebar() {
    isSidebarClosed = !isSidebarClosed;
    if (isSidebarClosed) {
        leftSidebar.style.transform = "translateX(-520px)"
        document.querySelectorAll(".directionalityFlip").forEach(el => el.style.transform = "rotate(180deg)")
        return
    }
    leftSidebar.style.transform = "translateX(0px)"
    document.querySelectorAll(".directionalityFlip").forEach(el => el.style.transform = "rotate(0deg)")
}

if (location.hostname === "localhost" || location.hostname === "127.0.0.1") {
    javaURI = "http://localhost:8085";
} else {
    javaURI = "https://spring2025.nighthawkcodingsociety.com";
}

let assignment = null;
let currentQueue = [];

let person;

document.getElementById('resetQueue').addEventListener('click', resetQueue);

let timerInterval;
let timerlength;
let queueUpdateInterval;

const URL = javaURI + "/api/assignments/"

async function fetchQueue() {
    const response = await fetch(URL + `getQueue/${assignment}`);
    if (response.ok) {
        const data = await response.json();
        updateQueueDisplay(data);
    }
}

async function fetchTimerLength() {
    console.log("test")
    const response = await fetch(URL + `getPresentationLength/${assignment}`);
    if (response.ok) {
        const data = await response.json();
        console.log(data);
        timerlength = data;
        document.getElementById('timerDisplay').textContent = `${Math.floor(timerlength / 60).toString().padStart(2, '0')}:${(timerlength % 60).toString().padStart(2, '0')}`;
    }
}

// add user to waiting
async function addToQueue() {
    await fetch(URL + `addToWaiting/${assignment}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify([person])
    });
    fetchQueue();
}

// remove user from waiting
async function removeFromQueue() {
    await fetch(URL + `removeToWorking/${assignment}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify([person])
    });
    fetchQueue();
}

// move user to completed
async function moveToDoneQueue() {
    const firstPerson = [currentQueue[0]];
    await fetch(URL + `doneToCompleted/${assignment}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(firstPerson)
    });
    fetchQueue();
}

// reset queue - todo: admin only
async function resetQueue() {
    await fetch(URL + `resetQueue/${assignment}`, {
        method: 'PUT'
    });
    fetchQueue();
}

// update display - ran periodically
function updateQueueDisplay(queue) {
    currentQueue = queue.waiting;

    const notGoneList = document.getElementById('notGoneList');
    const waitingList = document.getElementById('waitingList');
    const doneList = document.getElementById('doneList');

    notGoneList.innerHTML = queue.working.map(person => `<div class="card">${person}</div>`).join('');
    waitingList.innerHTML = queue.waiting.map(person => `<div class="card">${person}</div>`).join('');
    doneList.innerHTML = queue.completed.map(person => `<div class="card">${person}</div>`).join('');
}

document.getElementById('initializeQueue').addEventListener('click', initializeQueue);

// get assignments, used for initialization and popup connection
async function fetchAssignments() {
    console.log(URL + 'debug')
    const response = await fetch(URL + 'debug');
    if (response.ok) {
        const assignments = await response.json();
        const dropdown = document.getElementById('assignmentDropdown');
        dropdown.innerHTML = assignments.map(assignment =>
            `<option value="${assignment.id}">${assignment.name}</option>`
        ).join('');
    }
}

async function initializeQueue() {
    let peopleList;
    timerlength = document.getElementById("durationInput").value;
    const assignmentId = document.getElementById('assignmentDropdown').value;
    const checkedBoxes = [...document.querySelectorAll('#group-checkboxes input:checked')];
    const selectedGroupIds = checkedBoxes.map(cb => parseInt(cb.value));

    if (selectedGroupIds.length === 0) {
        alert("Please select at least one group.");
        return;
    }

    const response = await fetch('/api/groups');
    const allGroups = await response.json();

    const selectedGroups = allGroups.filter(group => selectedGroupIds.includes(group.id));

    const queueArray = selectedGroups.map(group =>
        group.members.map(member => member.name).join(' | ')
    );
    console.log(selectedGroups);
    console.log(queueArray);

    peopleList = queueArray;

    await fetch(URL + `initQueue/${assignmentId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify([peopleList, [timerlength]])
    });
    assignment = assignmentId;
    fetchQueue();
}

// Start the interval to periodically update the queue
function startQueueUpdateInterval(intervalInSeconds) {
    if (queueUpdateInterval) clearInterval(queueUpdateInterval); // Clear existing interval if any
    queueUpdateInterval = setInterval(() => {
        console.log("Updating queue...");
        fetchQueue();
        fetchTimerLength();
    }, intervalInSeconds * 1000);
}

// Stop the interval for queue updates if needed
function stopQueueUpdateInterval() {
    if (queueUpdateInterval) clearInterval(queueUpdateInterval);
}

window.addEventListener('load', () => {
    fetchUser();
    showAssignmentModal();
});

async function fetchUser() {
    const response = await fetch(javaURI + `/api/person/get`, {
        method: 'GET',
        cache: "no-cache",
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json',
            'X-Origin': 'client'
        }
    });
    if (response.ok) {
        const userInfo = await response.json();
        person = userInfo.name;
    }
}
function showAssignmentModal() {
    const modal = document.getElementById('assignmentModal');
    const modalDropdown = document.getElementById('modalAssignmentDropdown');

    // Fetch assignments and populate the dropdown
    fetchAssignments().then(() => {
        const dropdown = document.getElementById('assignmentDropdown');
        modalDropdown.innerHTML = dropdown.innerHTML; // Use the same data as the main dropdown
    });

    modal.style.display = 'block';

    // Add event listener for the confirm button
    document.getElementById('confirmAssignment').addEventListener('click', () => {
        const selectedAssignment = modalDropdown.value;
        document.getElementById("viewingAssignmentTitle").innerText = `Viewing Assigment: ${modalDropdown.options[selectedAssignment].text}`
        if (selectedAssignment) {
            assignment = selectedAssignment; // Set the global assignment variable
            fetchQueue();
            startQueueUpdateInterval(10);
            fetchTimerLength();
            modal.style.display = 'none';
        } else {
            alert('Please select an assignment.');
        }
    });
}

async function loadGroups() {
    const response = await fetch('/api/groups');
    const groups = await response.json();
    const container = document.getElementById('group-checkboxes');
    container.innerHTML = "<p>Select Groups:</p>";

    groups.forEach(group => {
        const label = document.createElement('label');
        label.innerHTML = `
                    <input type="checkbox" class="form-check-input" value="${group.id}" id="group-${group.id}"/>
                    <label class="form-check-label" for="group-${group.id}">
                        ${group.name}: ${group.members.map(m => m.name).join(', ')}
                    </label>
                `;
        container.appendChild(label);
        container.appendChild(document.createElement('br'));
    });
}

document.addEventListener('DOMContentLoaded', loadGroups);