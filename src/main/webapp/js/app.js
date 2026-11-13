document.addEventListener('DOMContentLoaded', function () {
    // Mobile sidebar toggle
    const sidebar = document.getElementById('appSidebar');
    const backdrop = document.getElementById('sidebarBackdrop');
    const toggleBtn = document.getElementById('sidebarToggleBtn');

    function closeSidebar() {
        if (sidebar) sidebar.classList.remove('show');
        if (backdrop) backdrop.classList.remove('show');
    }

    if (toggleBtn && sidebar) {
        toggleBtn.addEventListener('click', function () {
            sidebar.classList.toggle('show');
            if (backdrop) backdrop.classList.toggle('show');
        });
    }

    if (backdrop) {
        backdrop.addEventListener('click', closeSidebar);
    }

    // Live Present / Absent counter on Mark Attendance page
    updateAttendanceCounters();
    document.querySelectorAll('.attendance-status-radio').forEach(function (radio) {
        radio.addEventListener('change', updateAttendanceCounters);
    });
});

function updateAttendanceCounters() {
    const presentBadge = document.getElementById('livePresentCount');
    const absentBadge = document.getElementById('liveAbsentCount');
    if (!presentBadge || !absentBadge) return;

    let present = 0;
    let absent = 0;
    document.querySelectorAll('.attendance-status-radio:checked').forEach(function (radio) {
        if (radio.value === 'Present') present++;
        else if (radio.value === 'Absent') absent++;
    });

    presentBadge.textContent = present;
    absentBadge.textContent = absent;
}

function markAllAttendance(status) {
    document.querySelectorAll('.attendance-status-radio').forEach(function (radio) {
        if (radio.value === status) {
            radio.checked = true;
        }
    });
    updateAttendanceCounters();
}

function confirmDelete(formId, label) {
    if (confirm('Are you sure you want to delete ' + label + '? This action cannot be undone.')) {
        document.getElementById(formId).submit();
    }
}

function fillDemoCredentials(username, password) {
    const u = document.getElementById('username');
    const p = document.getElementById('password');
    if (u && p) {
        u.value = username;
        p.value = password;
        u.focus();
    }
}

