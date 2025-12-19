const API = "http://localhost:8080/api";

let chart = null;
let currentCoin = 'bitcoin';
let currentPeriod = '7d';
let prices = [];
let coinData = {};
let chartLoading = false;
let lastDataLoad = 0;

// Initialize
window.onload = async () => {
    try {
        await initializeApp();
        setInterval(loadPrices, 10000);
    } catch (error) {
        console.error('Init error:', error);
        showToast('App-Initialisierung fehlgeschlagen', 'error');
    }
};

async function initializeApp() {
    showLoading(true);
    await loadPrices();
    await loadPortfolio();
    showLoading(false);
}

function showLoading(show) {
    const loader = document.getElementById('global-loader');
    if (show) {
        loader.style.display = 'flex';
    } else {
        loader.style.display = 'none';
    }
}

// Toast Notification
function showToast(message, type = 'info') {
    const toast = document.getElementById('toast');
    const toastMessage = document.getElementById('toast-message');
    const icon = toast.querySelector('i');

    toastMessage.textContent = message;
    toast.className = `toast ${type}`;

    // Icons basierend auf Typ
    const icons = {
        'success': 'fas fa-check-circle',
        'error': 'fas fa-exclamation-circle',
        'warning': 'fas fa-exclamation-triangle',
        'info': 'fas fa-info-circle'
    };

    icon.className = icons[type] || 'fas fa-info-circle';

    toast.classList.add('show');

    setTimeout(() => {
        toast.classList.remove('show');
    }, 4000);
}

// Formatierung
function formatCurrency(amount) {
    if (amount === undefined || amount === null) return '--';
    if (amount < 0.01) return '$' + amount.toFixed(6);
    return new Intl.NumberFormat('de-DE', {
        style: 'currency',
        currency: 'USD',
        minimumFractionDigits: 2,
        maximumFractionDigits: 6
    }).format(amount);
}

function formatNumber(num, decimals = 4) {
    if (num === undefined || num === null) return '--';
    if (num === 0) return '0';
    return new Intl.NumberFormat('de-DE', {
        minimumFractionDigits: decimals,
        maximumFractionDigits: decimals
    }).format(num);
}

// Preise laden
async function loadPrices() {
    try {
        const now = Date.now();
        if (now - lastDataLoad < 5000) return; // Debounce

        const res = await fetch(`${API}/prices`);

        if (!res.ok) {
            throw new Error(`HTTP ${res.status}`);
        }

        const data = await res.json();

        if (!data || data.length === 0) {
            showNoPricesMessage();
            return;
        }

        prices = data;

        // Baue coinData Map
        coinData = {};
        data.forEach(coin => {
            const key = coin.name.toLowerCase();
            coinData[key] = {
                name: coin.name,
                price: coin.priceUsd,
                id: key
            };
        });

        updateCoinCards(data);
        updateTradeDropdown(data);

        // Auto-select Bitcoin wenn keiner ausgewählt
        if (!currentCoin && data.length > 0) {
            currentCoin = 'bitcoin';
            await loadChart(currentCoin, currentPeriod);
        }

        lastDataLoad = Date.now();

    } catch (error) {
        console.error('Error loading prices:', error);
        showNoPricesMessage();
    }
}

function showNoPricesMessage() {
    const container = document.getElementById("coin-cards");
    container.innerHTML = `
        <div class="no-data-message">
            <i class="fas fa-sync-alt fa-spin"></i>
            <p>Lade Echtzeit-Preise...</p>
            <small>Bitte warten oder Seite neu laden</small>
        </div>
    `;
}

// Coin Cards
function updateCoinCards(coins) {
    const container = document.getElementById("coin-cards");
    container.innerHTML = "";

    coins.forEach(coin => {
        const key = coin.name.toLowerCase();
        const isActive = currentCoin === key;
        const price = coin.priceUsd || 0;

        // Simuliere realistische 24h Änderung
        const change = (Math.random() * 8 - 4).toFixed(2);
        const isPositive = parseFloat(change) >= 0;

        const card = document.createElement("div");
        card.className = `coin-card ${isActive ? 'active' : ''}`;
        card.innerHTML = `
            <div class="coin-name">${coin.name}</div>
            <div class="coin-price">${formatCurrency(price)}</div>
            <div class="coin-change ${isPositive ? 'positive' : 'negative'}">
                <i class="fas fa-${isPositive ? 'arrow-up' : 'arrow-down'}"></i>
                ${isPositive ? '+' : ''}${change}%
            </div>
        `;

        card.onclick = async () => {
            if (chartLoading) return;

            document.querySelectorAll('.coin-card').forEach(c => c.classList.remove('active'));
            card.classList.add('active');

            currentCoin = key;
            await loadChart(currentCoin, currentPeriod);
        };

        container.appendChild(card);
    });
}

// Trade Dropdown
function updateTradeDropdown(coins) {
    const select = document.getElementById('tradeCoin');
    const currentValue = select.value;

    select.innerHTML = '<option value="">-- Coin wählen --</option>';

    coins.forEach(coin => {
        const option = document.createElement('option');
        const key = coin.name.toLowerCase();
        option.value = key;
        option.text = coin.name;

        if (key === currentValue || (!currentValue && key === 'bitcoin')) {
            option.selected = true;
        }

        select.appendChild(option);
    });

    calculateTradeTotal();
}

// Chart laden
async function loadChart(coin, period = '7d') {
    if (chartLoading) return;

    chartLoading = true;
    currentCoin = coin;
    currentPeriod = period;

    try {
        showChartLoading(true);

        const res = await fetch(`${API}/history?coin=${coin}&period=${period}`);

        if (!res.ok) {
            throw new Error(`HTTP ${res.status}`);
        }

        const data = await res.json();

        if (!data || data.length === 0) {
            showNoChartData();
            return;
        }

        renderChart(data, coin, period);

    } catch (error) {
        console.error('Chart error:', error);
        showNoChartData();
    } finally {
        chartLoading = false;
        showChartLoading(false);
    }
}

function showChartLoading(show) {
    const chartContainer = document.querySelector('.chart-container');
    if (show) {
        chartContainer.classList.add('loading');
    } else {
        chartContainer.classList.remove('loading');
    }
}

function showNoChartData() {
    const ctx = document.getElementById("priceChart");

    if (chart) {
        chart.destroy();
        chart = null;
    }

    ctx.innerHTML = `
        <div class="no-chart-data">
            <i class="fas fa-chart-line"></i>
            <p>Keine Chart-Daten verfügbar</p>
            <small>Bitte versuchen Sie es später erneut</small>
        </div>
    `;
}

// Chart rendern
function renderChart(historicalData, coin, period) {
    const ctx = document.getElementById("priceChart");
    ctx.innerHTML = ''; // Clear previous content

    if (chart) {
        chart.destroy();
        chart = null;
    }

    // Prepare data
    const labels = historicalData.map(d => {
        const date = new Date(d.timestamp);

        if (period === '7d') {
            return date.toLocaleDateString('de-DE', {
                day: '2-digit',
                month: 'short',
                hour: '2-digit'
            });
        } else {
            return date.toLocaleDateString('de-DE', {
                day: '2-digit',
                month: 'short'
            });
        }
    });

    const prices = historicalData.map(d => d.price);

    // Calculate trend
    const firstPrice = prices[0] || 0;
    const lastPrice = prices[prices.length - 1] || 0;
    const isPositive = lastPrice >= firstPrice;

    // Create chart
    chart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: `${coin.toUpperCase()} - ${getPeriodLabel(period)}`,
                data: prices,
                borderColor: isPositive ? '#10b981' : '#ef4444',
                backgroundColor: isPositive ? 'rgba(16, 185, 129, 0.1)' : 'rgba(239, 68, 68, 0.1)',
                borderWidth: 3,
                fill: true,
                tension: 0.4,
                pointRadius: 0,
                pointHoverRadius: 4,
                pointHitRadius: 10
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: true,
                    labels: {
                        color: '#f1f5f9',
                        font: { size: 14 }
                    }
                },
                tooltip: {
                    mode: 'index',
                    intersect: false,
                    backgroundColor: 'rgba(30, 41, 59, 0.95)',
                    titleColor: '#f1f5f9',
                    bodyColor: '#f1f5f9',
                    borderColor: isPositive ? '#10b981' : '#ef4444',
                    callbacks: {
                        label: ctx => `$${ctx.raw.toFixed(2)}`
                    }
                }
            },
            scales: {
                x: {
                    grid: { color: 'rgba(255, 255, 255, 0.1)' },
                    ticks: {
                        color: '#94a3b8',
                        maxRotation: 45,
                        font: { size: 11 }
                    }
                },
                y: {
                    grid: { color: 'rgba(255, 255, 255, 0.1)' },
                    ticks: {
                        color: '#94a3b8',
                        callback: value => {
                            if (value >= 1000) return '$' + (value/1000).toFixed(1) + 'k';
                            return '$' + value;
                        }
                    }
                }
            },
            animation: {
                duration: 800
            }
        }
    });
}

function getPeriodLabel(period) {
    return period === '7d' ? '7 Tage' : period === '14d' ? '14 Tage' : '30 Tage';
}

// Portfolio
async function loadPortfolio() {
    try {
        const res = await fetch(`${API}/portfolio`);

        if (!res.ok) {
            throw new Error(`HTTP ${res.status}`);
        }

        const data = await res.json();

        updateBalance(data.balance);
        updatePortfolioTable(data.holdings);

    } catch (error) {
        console.error('Portfolio error:', error);
        updatePortfolioTable({});
    }
}

function updateBalance(balance) {
    document.getElementById('balance').textContent = formatNumber(balance, 2);
}

function updatePortfolioTable(holdings) {
    const tbody = document.getElementById("portfolio-body");

    if (!holdings || Object.keys(holdings).length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" class="empty-portfolio">
                    <i class="fas fa-wallet"></i>
                    <p>Keine Coins im Portfolio</p>
                    <small>Kaufen Sie Ihre ersten Coins!</small>
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = '';

    Object.entries(holdings).forEach(([coin, amount]) => {
        const coinInfo = coinData[coin.toLowerCase()];
        const price = coinInfo?.price || 0;
        const value = price * amount;
        const change = (Math.random() * 8 - 4).toFixed(2);
        const isPositive = parseFloat(change) >= 0;

        const row = document.createElement('tr');
        row.innerHTML = `
            <td><strong>${coin.toUpperCase()}</strong></td>
            <td>${formatNumber(amount)}</td>
            <td>${formatCurrency(price)}</td>
            <td class="holding-value">${formatCurrency(value)}</td>
            <td>
                <span class="holding-change ${isPositive ? 'positive' : 'negative'}">
                    <i class="fas fa-${isPositive ? 'arrow-up' : 'arrow-down'}"></i>
                    ${isPositive ? '+' : ''}${change}%
                </span>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Trading
calculateTradeTotal = () => {
    const coin = document.getElementById('tradeCoin').value;
    const amount = parseFloat(document.getElementById('tradeAmount').value) || 0;

    if (coin && amount > 0) {
        const price = coinData[coin]?.price || 0;
        document.getElementById('tradeTotal').textContent = formatCurrency(price * amount);
    } else {
        document.getElementById('tradeTotal').textContent = '$0.00';
    }
};

async function buy() {
    await executeTrade('buy');
}

async function sell() {
    await executeTrade('sell');
}

async function executeTrade(type) {
    const coin = document.getElementById('tradeCoin').value;
    const amount = parseFloat(document.getElementById('tradeAmount').value);

    if (!coin) {
        showToast('Bitte Coin auswählen', 'error');
        return;
    }

    if (!amount || amount <= 0) {
        showToast('Bitte gültige Menge eingeben', 'error');
        return;
    }

    const btn = document.getElementById(type + 'Btn');
    const originalHtml = btn.innerHTML;

    btn.innerHTML = '<div class="spinner"></div>';
    btn.disabled = true;

    try {
        const res = await fetch(`${API}/portfolio/${type}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ coin, amount })
        });

        if (!res.ok) {
            const error = await res.text();
            throw new Error(error || `${type} fehlgeschlagen`);
        }

        showToast(`${formatNumber(amount)} ${coin.toUpperCase()} erfolgreich ${type === 'buy' ? 'gekauft' : 'verkauft'}!`, 'success');
        document.getElementById('tradeAmount').value = '';

        await Promise.all([loadPortfolio(), loadPrices()]);

    } catch (error) {
        showToast(error.message, 'error');
    } finally {
        btn.innerHTML = originalHtml;
        btn.disabled = false;
        calculateTradeTotal();
    }
}

// Event Listeners
document.getElementById('buyBtn').onclick = buy;
document.getElementById('sellBtn').onclick = sell;
document.getElementById('tradeAmount').addEventListener('input', calculateTradeTotal);
document.getElementById('tradeCoin').addEventListener('change', function() {
    if (this.value) {
        loadChart(this.value, currentPeriod);
    }
    calculateTradeTotal();
});

// Period Buttons
document.querySelectorAll('.period-btn').forEach(btn => {
    btn.onclick = function() {
        document.querySelectorAll('.period-btn').forEach(b => b.classList.remove('active'));
        this.classList.add('active');
        currentPeriod = this.dataset.period;
        if (currentCoin) {
            loadChart(currentCoin, currentPeriod);
        }
    };
});

// Add global loader to HTML
document.body.insertAdjacentHTML('beforeend', `
<div id="global-loader">
    <div class="loader-content">
        <div class="spinner-large"></div>
        <p>Lade Krypto-Daten...</p>
    </div>
</div>
`);
// Loading State Management
function showLoading(show) {
    const loader = document.getElementById('global-loader');
    if (show) {
        loader.style.display = 'flex';
    } else {
        loader.style.display = 'none';
    }
}

function showChartLoading(show) {
    const chartContainer = document.querySelector('.chart-container');
    if (show) {
        chartContainer.classList.add('loading');
    } else {
        chartContainer.classList.remove('loading');
    }
}

// No Data Messages
function showNoPricesMessage() {
    const container = document.getElementById("coin-cards");
    container.innerHTML = `
        <div class="no-data-message">
            <i class="fas fa-sync-alt fa-spin"></i>
            <p>Lade Echtzeit-Preise...</p>
            <small>Bitte warten oder Seite neu laden</small>
        </div>
    `;
}

function showNoChartData() {
    const ctx = document.getElementById("priceChart");

    if (chart) {
        chart.destroy();
        chart = null;
    }

    ctx.innerHTML = `
        <div class="no-chart-data">
            <i class="fas fa-chart-line"></i>
            <p>Keine Chart-Daten verfügbar</p>
            <small>Bitte versuchen Sie es später erneut</small>
        </div>
    `;
}