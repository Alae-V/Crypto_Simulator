 Crypto Simulator — Smart Cryptocurrency Trading Platform
Crypto Simulator is a Java-based cryptocurrency trading simulation platform that uses real-time market data from CoinAPI to provide a realistic trading experience without financial risk.

🚀 Features
🕵️ Detects real-time cryptocurrency price changes
🧠 Uses CoinAPI to fetch live market data
💰 Virtual trading with 100,000€ starting capital
📊 Sends portfolio performance insights
⚙️ User-defined trading strategies
🖥️ Modern Swing-based dashboard with chart visualization
💾 Stores all data in CSV files for persistence

🧩 Tech Stack
Component	Technology
Backend	Java 17
GUI	Java Swing
Data Fetching	CoinAPI REST API
Data Storage	CSV files
Build Tool	Maven
Charts	Custom Swing components
⚙️ Setup Instructions
1. Clone the repository
git clone https://github.com/Alae-V/Crypto_Simulator.git
cd Crypto_Simulator
2. Configure API Key
Before running the project, get a free API key from CoinAPI (https://www.coinapi.io/) and either:

Set as environment variable:

export COINAPI_KEY="your-api-key-here"
Or edit directly in code:
Open src/main/java/controller/CoinDataFetcher.java and update the API_KEY constant.

3. Run the Application
Run the Java application using Maven:

mvn compile
mvn exec:java -Dexec.mainClass="view.MainFrame"
Then open the application window.

🎮 Usage
Add cryptocurrencies you want to track via the GUI

Specify trading amounts and strategies

Click "Kaufen" or "Verkaufen" to execute trades

Monitor portfolio performance in real-time charts

View transaction history and export data

🛠️ Development Notes
The project uses CSV files for data persistence

All market data is fetched from CoinAPI in real-time

The application follows MVC architecture pattern

You can extend the Coin class to add new cryptocurrencies

💡 Trading Tips
Start with small amounts to test strategies

Monitor market trends before large trades

Use the historical data feature to backtest strategies

Diversify your virtual portfolio across different cryptocurrencies

📄 License
You are free to use, modify, and distribute this project with attribution.

✨ Created by Alae Ben Salah
