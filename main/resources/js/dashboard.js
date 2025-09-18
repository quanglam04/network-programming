    let startTime = new Date();
    let lastRequestCount = 0;
    let requestHistory = [];

    function updateDashboard() {
    fetch('/api/stats')
        .then(response => response.json())
        .then(data => {
            // Update total requests
            document.getElementById('total-requests').textContent = data.totalRequests;

            // Update current time
            document.getElementById('current-time').textContent = data.currentTime;

            // Update uptime
            const uptime = Math.floor((new Date() - startTime) / 1000);
            const hours = Math.floor(uptime / 3600);
            const minutes = Math.floor((uptime % 3600) / 60);
            const seconds = uptime % 60;
            document.getElementById('uptime').textContent =
                `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;

            // Calculate requests per minute
            requestHistory.push({time: new Date(), count: data.totalRequests});
            // Keep only last 10 minutes of data
            const tenMinutesAgo = new Date(Date.now() - 10 * 60 * 1000);
            requestHistory = requestHistory.filter(entry => entry.time > tenMinutesAgo);

            let reqPerMin = 0;
            if (requestHistory.length > 1) {
                const firstEntry = requestHistory[0];
                const lastEntry = requestHistory[requestHistory.length - 1];
                const timeDiff = (lastEntry.time - firstEntry.time) / (1000 * 60); // minutes
                const requestDiff = lastEntry.count - firstEntry.count;
                reqPerMin = timeDiff > 0 ? Math.round(requestDiff / timeDiff) : 0;
            }
            document.getElementById('requests-per-min').textContent = reqPerMin;

            // Update logs
            const logsContainer = document.getElementById('logs-list');
            if (data.logs && data.logs.length > 0) {
                logsContainer.innerHTML = data.logs.map(log => {
                    let className = 'log-entry';
                    if (log.includes('ERROR') || log.includes('502 Bad Gateway')) {
                        className += ' error';
                    } else if (log.includes('CONNECT')) {
                        className += ' connect';
                    }
                    return `<div class="${className}">${log}</div>`;
                }).join('');
            } else {
                logsContainer.innerHTML = '<div class="log-entry">No requests yet...</div>';
            }

            // Update last update time
            document.getElementById('last-update').textContent = new Date().toLocaleTimeString();
        })
        .catch(error => {
            console.error('Error fetching stats:', error);
            document.getElementById('last-update').textContent = 'Error - ' + new Date().toLocaleTimeString();
        });
}

    // Initial update
    updateDashboard();

    // Update every 2 seconds
    setInterval(updateDashboard, 1000);
