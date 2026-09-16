document.addEventListener('DOMContentLoaded', () => {
  const deptCanvas = document.getElementById('deptChart');
  const catCanvas = document.getElementById('categoryChart');
  const trendCanvas = document.getElementById('trendChart');
  const statusCanvas = document.getElementById('statusChart');

  if (deptCanvas && catCanvas && trendCanvas && statusCanvas) {
    fetch('/api/dashboard/charts')
      .then(res => res.json())
      .then(data => {
        new Chart(deptCanvas.getContext('2d'), {
          type: 'bar',
          data: {
            labels: data.departments.labels,
            datasets: [{
              label: 'Bookings',
              data: data.departments.values,
              backgroundColor: '#4f46e5',
              borderRadius: 6
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
              y: { beginAtZero: true, ticks: { precision: 0 } }
            }
          }
        });

        new Chart(catCanvas.getContext('2d'), {
          type: 'doughnut',
          data: {
            labels: data.categories.labels,
            datasets: [{
              data: data.categories.values,
              backgroundColor: ['#4f46e5', '#0ea5e9', '#10b981', '#f59e0b', '#ec4899', '#8b5cf6'],
              borderWidth: 2
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: { position: 'bottom', labels: { boxWidth: 12, font: { size: 11 } } }
            }
          }
        });

        new Chart(trendCanvas.getContext('2d'), {
          type: 'line',
          data: {
            labels: data.monthly_trends.labels,
            datasets: [{
              label: 'Monthly Bookings',
              data: data.monthly_trends.values,
              borderColor: '#0ea5e9',
              backgroundColor: 'rgba(14, 165, 233, 0.1)',
              fill: true,
              tension: 0.35,
              pointBackgroundColor: '#0ea5e9'
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
              y: { beginAtZero: true, ticks: { precision: 0 } }
            }
          }
        });

        new Chart(statusCanvas.getContext('2d'), {
          type: 'pie',
          data: {
            labels: data.status_distribution.labels,
            datasets: [{
              data: data.status_distribution.values,
              backgroundColor: ['#f59e0b', '#10b981', '#ef4444', '#64748b', '#8b5cf6'],
              borderWidth: 2
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: { position: 'bottom', labels: { boxWidth: 12, font: { size: 11 } } }
            }
          }
        });
      })
      .catch(err => console.error('Dashboard charts error:', err));
  }

  const reportResCanvas = document.getElementById('reportResourceChart');
  const reportDeptCanvas = document.getElementById('reportDeptChart');
  const reportRoomCanvas = document.getElementById('reportRoomChart');

  if (reportResCanvas || reportDeptCanvas || reportRoomCanvas) {
    fetch('/api/reports/chart-data')
      .then(res => res.json())
      .then(data => {
        if (reportResCanvas && data.resources) {
          new Chart(reportResCanvas.getContext('2d'), {
            type: 'bar',
            data: {
              labels: data.resources.labels,
              datasets: [{
                label: 'Total Bookings',
                data: data.resources.data,
                backgroundColor: '#6366f1',
                borderRadius: 4
              }]
            },
            options: {
              indexAxis: 'y',
              responsive: true,
              maintainAspectRatio: false,
              plugins: { legend: { display: false } },
              scales: { x: { beginAtZero: true, ticks: { precision: 0 } } }
            }
          });
        }

        if (reportDeptCanvas && data.departments) {
          new Chart(reportDeptCanvas.getContext('2d'), {
            type: 'polarArea',
            data: {
              labels: data.departments.labels,
              datasets: [{
                data: data.departments.data,
                backgroundColor: ['#4f46e5', '#0ea5e9', '#10b981', '#f59e0b', '#ec4899', '#8b5cf6']
              }]
            },
            options: {
              responsive: true,
              maintainAspectRatio: false,
              plugins: { legend: { position: 'bottom' } }
            }
          });
        }

        if (reportRoomCanvas && data.rooms) {
          new Chart(reportRoomCanvas.getContext('2d'), {
            type: 'bar',
            data: {
              labels: data.rooms.labels,
              datasets: [{
                label: 'Room Bookings',
                data: data.rooms.data,
                backgroundColor: '#10b981',
                borderRadius: 4
              }]
            },
            options: {
              responsive: true,
              maintainAspectRatio: false,
              plugins: { legend: { display: false } },
              scales: { y: { beginAtZero: true, ticks: { precision: 0 } } }
            }
          });
        }
      })
      .catch(err => console.error('Reports charts error:', err));
  }
});
