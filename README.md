# 🌐 Crypto Simulator — Smart Cryptocurrency Trading Platform

Crypto Simulator is a **Java-based** cryptocurrency trading simulation platform that uses real-time market data from **CoinAPI** to provide a realistic trading experience without financial risk.  
It features **virtual portfolio management**, **real-time chart visualization**, and **CSV data persistence** for tracking your trading performance.
 
## 🚀 Features

- 📊 Real-time cryptocurrency price monitoring  
- 💰 Virtual trading with 100,000€ starting capital  
- 📈 Portfolio management with transaction history  
- 🖥️ Java Swing GUI with chart visualization  
- 💾 CSV data storage for persistence  
- 🔄 Automatic price updates every 5 minutes  

## 🧩 Tech Stack

| Component | Technology |
|------------|-------------|
| Backend | Java 17 |
| GUI | Java Swing |
| Data Fetching | CoinAPI REST API |
| Data Storage | CSV files |
| Build Tool | Maven |
| Architecture | MVC Pattern |

## ⚙️ Setup Instructions

### Clone the repository

git clone https://github.com/Alae-V/Crypto_Simulator.git
cd Crypto_Simulator

---

### Configure API Key

Before running the project, get a free API key from [CoinAPI](https://www.coinapi.io/).

**Option A — Environment Variable:**

export COINAPI_KEY="your-api-key-here"

**Option B — Edit Source Code:**

Open `src/main/java/controller/CoinDataFetcher.java` and update the API_KEY constant.

## ⚠️ Important Notes:

* The free CoinAPI plan has limited requests per day
* For unlimited access, consider upgrading to a paid plan
* Always test with small amounts when developing trading strategies

---

### ▶️ Running the App

### Run the Project

Run the Java application using Maven:

mvn compile
mvn exec:java -Dexec.mainClass="view.MainFrame"

Then use the application window to start trading.

---

## 🧠 Usage

1. Launch the Crypto Simulator application  
2. Browse available cryptocurrencies in the market view  
3. Select a cryptocurrency and enter your trade amount  
4. Click **"Kaufen"** to buy or **"Verkaufen"** to sell  
5. Monitor your portfolio performance in real-time  
6. Export transaction history as CSV for analysis  

## 🧰 Development Notes

- The project uses **CSV files** for data persistence in the resources folder  
- All market data is fetched from **CoinAPI** in real-time  
- The application follows the **MVC architecture** pattern  
- You can extend the Coin class to add new cryptocurrency types  

## 📊 Trading Tips

| Tip | Explanation |
|------|-------------|
| Start small | Test strategies with small amounts first |
| Diversify | Spread investments across different cryptocurrencies |
| Track performance | Regularly review your portfolio's performance |
| Use historical data | Analyze past trends to inform future trades |

---

You are free to use, modify, and distribute this project with attribution.

---

### ✨ Created by Alae Ben Salah
