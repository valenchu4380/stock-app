// dashboard.js — Chart.js dashboard charts
// Must load AFTER Chart.js CDN (chart.umd.min.js)
// Data is passed via inline script with th:inline="javascript"

window.initDashboardCharts = function(data) {
    if (!data || !data.labels || !data.labels.length) return;

    // Chart 1: Ganancia por producto (bar)
    new Chart(document.getElementById('chartGanancias'), {
        type: 'bar',
        data: {
            labels: data.labels,
            datasets: [{
                label: 'Ganancia ($)',
                data: data.ganancias,
                backgroundColor: data.ganancias.map(function(v) { return v >= 0 ? 'rgba(125, 158, 120, .75)' : 'rgba(220, 38, 38, .75)'; }),
                borderColor: data.ganancias.map(function(v) { return v >= 0 ? '#7D9E78' : '#dc2626'; }),
                borderWidth: 1,
                borderRadius: 4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: function(ctx) { return '$' + ctx.raw.toFixed(2); }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: { callback: function(v) { return '$' + v.toFixed(0); } }
                }
            }
        }
    });

    // Chart 2: Costo vs Venta (grouped bar)
    new Chart(document.getElementById('chartCostoVenta'), {
        type: 'bar',
        data: {
            labels: data.labels,
            datasets: [
                {
                    label: 'Costo total ($)',
                    data: data.costos,
                    backgroundColor: 'rgba(220, 38, 38, .7)',
                    borderColor: '#dc2626',
                    borderWidth: 1,
                    borderRadius: 4
                },
                {
                    label: 'Venta total ($)',
                    data: data.ventas,
                    backgroundColor: 'rgba(201, 123, 123, .7)',
                    borderColor: '#C97B7B',
                    borderWidth: 1,
                    borderRadius: 4
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                tooltip: {
                    callbacks: {
                        label: function(ctx) { return ctx.dataset.label + ': $' + ctx.raw.toFixed(2); }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: { callback: function(v) { return '$' + v.toFixed(0); } }
                }
            }
        }
    });

    // Chart 3: Margen de ganancia (bar)
    new Chart(document.getElementById('chartMargenes'), {
        type: 'bar',
        data: {
            labels: data.labels,
            datasets: [{
                label: 'Margen (%)',
                data: data.margenes,
                backgroundColor: data.margenes.map(function(v) { return v >= 30 ? 'rgba(125, 158, 120, .75)' : v >= 15 ? 'rgba(217, 119, 6, .75)' : 'rgba(220, 38, 38, .75)'; }),
                borderColor: data.margenes.map(function(v) { return v >= 30 ? '#7D9E78' : v >= 15 ? '#d97706' : '#dc2626'; }),
                borderWidth: 1,
                borderRadius: 4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: function(ctx) { return ctx.raw.toFixed(1) + '%'; }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: { callback: function(v) { return v.toFixed(0) + '%'; } }
                }
            }
        }
    });

    // Chart 4: Ganancia por categoría (doughnut)
    if (data.catLabels && data.catLabels.length) {
        var colores = ['#C97B7B', '#7D9E78', '#d97706', '#7c3aed', '#dc2626', '#0891b2'];
        new Chart(document.getElementById('chartCat'), {
            type: 'doughnut',
            data: {
                labels: data.catLabels,
                datasets: [{
                    data: data.catGanancias,
                    backgroundColor: data.catLabels.map(function(_, i) { return colores[i % colores.length]; }),
                    borderWidth: 2,
                    borderColor: '#fff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { position: 'bottom', labels: { padding: 16, font: { size: 13 } } },
                    tooltip: {
                        callbacks: {
                            label: function(ctx) {
                                var total = data.catGanancias.reduce(function(a, b) { return a + b; }, 0);
                                var pct = ((ctx.raw / total) * 100).toFixed(1);
                                return ctx.label + ': $' + ctx.raw.toFixed(2) + ' (' + pct + '%)';
                            }
                        }
                    }
                }
            }
        });
    }

    // Chart 5: Ganancia por línea (horizontal bar)
    if (data.lineaLabels && data.lineaLabels.length) {
        var colores2 = ['#C97B7B', '#7D9E78', '#d97706', '#7c3aed', '#dc2626', '#0891b2', '#ea580c', '#be185d'];
        new Chart(document.getElementById('chartLinea'), {
            type: 'bar',
            data: {
                labels: data.lineaLabels,
                datasets: [{
                    label: 'Ganancia ($)',
                    data: data.lineaGanancias,
                    backgroundColor: data.lineaGanancias.map(function(v) { return v >= 0 ? 'rgba(125, 158, 120, .75)' : 'rgba(220, 38, 38, .75)'; }),
                    borderColor: data.lineaGanancias.map(function(v) { return v >= 0 ? '#7D9E78' : '#dc2626'; }),
                    borderWidth: 1,
                    borderRadius: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                indexAxis: 'y',
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        callbacks: {
                            label: function(ctx) { return '$' + ctx.raw.toFixed(2); }
                        }
                    }
                },
                scales: {
                    x: { beginAtZero: true, ticks: { callback: function(v) { return '$' + v.toFixed(0); } } }
                }
            }
        });
    }
};
