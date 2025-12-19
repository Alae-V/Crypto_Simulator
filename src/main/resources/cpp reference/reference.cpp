#include <curl/curl.h>

#include <cstdlib>
#include <ctime>
#include <iostream>
#include <map>
#include <string>
#include <vector>
#include <fstream>

#include "json.hpp"
using json = nlohmann::json;

struct Crypto {
    std::string name;
    double price;
    std::vector<double> historicalPrices;
    const char* url;
};

struct Portfolio {
    double balance;
    std::map<std::string, double> holdings;
};

std::string crypto_adjustedname(std::string firstname) {
    if (firstname == "Bitcoin") {
        return "bitcoin";
    }
    else if (firstname == "Ethereum") {
        return "ethereum";
    }
    else if (firstname == "Tether") {
        return "tether";
    }
    else if (firstname == "Solana") {
        return "solana";
    }
    else if (firstname == "BNB") {
        return "binancecoin";
    }
    else if (firstname == "XRP") {
        return "ripple";
    }
    else if (firstname == "Cardano") {
        return "cardano";
    }
    else if (firstname == "Dogecoin") {
        return "dogecoin";
    }
    return "";
}
double generateRandomPrice(double minPrice, double maxPrice) {
    return minPrice +
        static_cast<double>(rand()) / RAND_MAX * (maxPrice - minPrice);
}
size_t WriteCallback(void* contents, size_t size, size_t nmemb,
    std::string* output) {
    size_t totalSize = size * nmemb;
    output->append((char*)contents, totalSize);
    return totalSize;
}


void get_realtime_prices( std::vector<Crypto>& cryptos) {
    CURL* curl;
    CURLcode res;
    std::string readbuffer;
    double realtime_price = 0.0;
    const char* URL =
        "https://api.coingecko.com/api/v3/simple/"
        "price?ids=bitcoin,ethereum,tether,solana,binancecoin,ripple,"
		"cardano,dogecoin&vs_currencies=usd";

    curl_global_init(CURL_GLOBAL_DEFAULT);
    curl = curl_easy_init();

    if (curl) {
        curl_easy_setopt(curl, CURLOPT_URL, URL);
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, WriteCallback);
        curl_easy_setopt(curl, CURLOPT_WRITEDATA, &readbuffer);

        res = curl_easy_perform(curl);

        if (res != CURLE_OK) {
            std::cerr << "cURL Fehler: " << curl_easy_strerror(res) << std::endl;
        }
        else {
            try {
                json data = json::parse(readbuffer);


                for (auto& crypto : cryptos) {
                    if (data.contains(crypto_adjustedname(crypto.name)) && data[crypto_adjustedname(crypto.name)].contains("usd")) {
                        if (data[crypto_adjustedname(crypto.name)]["usd"].is_number()) {
                            realtime_price = data[crypto_adjustedname(crypto.name)]["usd"].get<double>();
                            crypto.price = realtime_price;
                            crypto.historicalPrices.push_back(crypto.price);
                        }
                        else {
                            std::cerr << "⚠️ Typfehler: " << crypto_adjustedname(crypto.name) << " → "
                                << "usd" << " ist kein Zahlentyp!\n";
                        }
                    }
                    else {
                        std::cerr << "⚠️ Unerwartetes JSON-Format: Coin oder Currency fehlt!\n";
                    }

                }
            } catch (const std::exception& e) {
                std::cerr << "Fehler: " << e.what() << std::endl;
            }
        }

        curl_easy_cleanup(curl);
    }
    curl_global_cleanup();

}

void updatePrices(std::vector<Crypto>& cryptos) {

     get_realtime_prices(cryptos);

}

double findPrice(const std::string& name, const std::vector<Crypto>& cryptos) {
    for (const auto& crypto : cryptos) {
        if (crypto.name == name) return crypto.price;
    }
    return 0.0;
}

void displayPrices(const std::vector<Crypto>& cryptos) {
    for (const auto& crypto : cryptos) {
        std::cout << crypto.name << ": $" << crypto.price << "\n";
    }
}

void displayHistoricalPrices(const std::vector<Crypto>& cryptos) {
    for (const auto& crypto : cryptos) {
        std::cout << "\nHistorische Preise fuer " << crypto.name << ": ";
        for (const auto& price : crypto.historicalPrices) {
            std::cout << price << " | ";
        }
        std::cout << "\n";
    }
}

void save_data(const std::string& filename, const Portfolio& portfolio,const std::vector<Crypto>& cryptos) {

    json j;
    j["portfolio"]["balance"] = portfolio.balance;
    for (const auto& [coin, amount] : portfolio.holdings) {
        j["portfolio"]["holdings"][coin] = amount;
    }
    for (const auto& crypto : cryptos) {
		j["Cryptos"][crypto.name]["historicalPrices"] = crypto.historicalPrices;
    }
    std::ofstream file(filename);

    if (file.is_open()) {
        file << j.dump(4);
		std::cout << "Daten erfolgreich gespeichert in " << filename << "\n";
    }
    else {
		std::cerr << "Fehler beim Oeffnen der Datei zum Speichern!\n";
    }


}
void load_data(const std::string& filename, Portfolio& portfolio, std::vector<Crypto>& cryptos) {

    json j;
    std::ifstream file(filename);
    if (!file.is_open()) {
		std::cerr << "Fehler beim Oeffnen der Datei zum Laden!\n";
    }
    file >> j;
    portfolio.balance = j["portfolio"]["balance"].get<double>();
    for (const auto& [coin,amount] : j["portfolio"]["holdings"].items()) {
        portfolio.holdings[coin] = amount.get<double>();
    }
    for (const auto& [name, data] : j["Cryptos"].items()){

        for (auto& crypto : cryptos) {
            if (crypto.name == name) {
                crypto.historicalPrices = data["historicalPrices"].get<std::vector<double>>();
            };
        }

    }
	std::cout << "Daten erfolgreich geladen aus " << filename << "\n";

}

int main() {
    srand(static_cast<unsigned int>(time(0)));
    const char* url_Bitcoin =
        "https://api.coingecko.com/api/v3/simple/"
        "price?ids=bitcoin&vs_currencies=usd";
    const char* url_Ethereum =
        "https://api.coingecko.com/api/v3/simple/"
        "price?ids=ethereum&vs_currencies=usd";
    const char* url_Tether =
        "https://api.coingecko.com/api/v3/simple/"
        "price?ids=tether&vs_currencies=usd";
    const char* url_Solana =
        "https://api.coingecko.com/api/v3/simple/"
        "price?ids=solana&vs_currencies=usd";
    const char* url_BNB =
        "https://api.coingecko.com/api/v3/simple/"
        "price?ids=binancecoin&vs_currencies=usd";
    const char* url_XRP =
        "https://api.coingecko.com/api/v3/simple/"
        "price?ids=ripple&vs_currencies=usd";
    const char* url_Cardano =
        "https://api.coingecko.com/api/v3/simple/"
        "price?ids=cardano&vs_currencies=usd";
    const char* url_Dogecoin =
        "https://api.coingecko.com/api/v3/simple/"
        "price?ids=dogecoin&vs_currencies=usd";

    std::vector<Crypto> cryptos = {
        {"Bitcoin",   0.0,   {},  url_Bitcoin},
        {"Ethereum",  0.0,   {},  url_Ethereum},
        {"Tether",   0.0,   {},   url_Tether},
        {"Solana",   0.0, {},    url_Solana},
        {"BNB", 0.0, {}, url_BNB},
        {"XRP", 0.0, {}, url_XRP},
        {"Cardano", 0.0,  {},   url_Cardano},
        {"Dogecoin",   0.0,   {},  url_Dogecoin} };
	get_realtime_prices(cryptos);

    Portfolio userPortfolio = { 10000, {} };
    char choice;

    do {
        std::cout << "\n--- Menue ---\n";
        std::cout << "1: Aktuelle Preise anzeigen\n";
        std::cout << "2: Kaufen\n";
        std::cout << "3: Verkaufen\n";
        std::cout << "4: Portfolio anzeigen\n";
        std::cout << "5: Historische Preise anzeigen\n";
		std::cout << "6: Daten speichern\n";
		std::cout << "7: Daten laden\n";
        std::cout << "8: Beenden\n";
        std::cin >> choice;

        switch (choice) {
        case '1':
            updatePrices(cryptos);
            displayPrices(cryptos);
            break;

        case '2': {
            int buyChoice;
            double amountToBuy;
            std::string end_choice;
            std::cout << "Waehle eine Krypto zum Kaufen:\n";
            for (size_t i = 0; i < cryptos.size(); i++) {
                std::cout << i + 1 << ". " << cryptos[i].name << ": $"
                    << cryptos[i].price << "\n";
            }
            std::cin >> buyChoice;
            if (buyChoice < 1 || buyChoice > static_cast<int>(cryptos.size()))
                break;
            Crypto& selectedCrypto = cryptos[buyChoice - 1];
            std::cout << "Wie viel moechten Sie kaufen?\n";
            std::cin >> amountToBuy;
            double cost = amountToBuy * selectedCrypto.price;
            std::cout<< "Wollen Sie " << amountToBuy << " Einheiten von " << selectedCrypto.name << " fuer $" << cost << "  kaufen ?(j/n)" << "\n";
			std::cin >> end_choice;
            if (end_choice == "n") {
                break;
            } else if( end_choice != "j") {
                std::cout << "Ungueltige Eingabe.\n";
                break;
			}

            if (cost > userPortfolio.balance) {
                std::cout << "Nicht genug Guthaben!\n";
                break;
            }
            userPortfolio.balance -= cost;
            userPortfolio.holdings[selectedCrypto.name] += amountToBuy;
            std::cout << "Gekauft: " << amountToBuy << " " << selectedCrypto.name
                << " fuer $" << cost << "\n";
            break;
        }

        case '3': {
            std::string end_choice;
            if (userPortfolio.holdings.empty()) {
                std::cout << "Keine Kryptowaehrungen im Portfolio.\n";
                break;
            }
            std::cout << "Welche Krypto moechten Sie verkaufen?\n";
            int sellChoice, index = 1;
            double amountToSell;
            for (const auto& holding : userPortfolio.holdings) {
                std::cout << index++ << ". " << holding.first
                    << " Menge: " << holding.second << "\n";
            }
            std::cin >> sellChoice;
            if (sellChoice < 1 ||
                sellChoice > static_cast<int>(userPortfolio.holdings.size()))
                break;
            auto it = userPortfolio.holdings.begin();
            std::advance(it, sellChoice - 1);
            std::cout << "Wie viel moechten Sie verkaufen?\n";
            std::cin >> amountToSell;
            if (amountToSell < 1 || amountToSell > it->second) {
                std::cout << "Ungueltige Menge.\n";
                break;
            }
            double sellPrice = findPrice(it->first, cryptos);
			std::cout << "Wollen Sie " << amountToSell << " Einheiten von " << it->first << " fuer $" << sellPrice <<"  verkaufen ? (j/n)" << "\n";
            std::cin >> end_choice;
            if(end_choice == "n") {
                break;
			}
            else if (end_choice != "j") {
                std::cout << "Ungueltige Eingabe.\n";
                break;
            }

            userPortfolio.balance += amountToSell * sellPrice;
            it->second -= amountToSell;
            if (it->second == 0) userPortfolio.holdings.erase(it);
            std::cout << "Verkauft: " << amountToSell << " " << it->first
                << " fuer $" << (amountToSell * sellPrice) << "\n";
            break;
        }

        case '4':
            std::cout << "\n--- Dein Portfolio ---\n";
            std::cout << "Kontostand: $" << userPortfolio.balance << "\n";
            for (const auto& holding : userPortfolio.holdings) {
                std::cout << holding.first << ": " << holding.second << "\n";
            }
            break;

        case '5':
            displayHistoricalPrices(cryptos);
            break;

		case '6':
			save_data("save_file.json", userPortfolio, cryptos);
            break;

        case '7':
            load_data("save_file.json", userPortfolio, cryptos);
			break;

        case '8':
            std::cout << "Programm beendet. Danke fuers Spielen!\n";
            break;
        }
    } while (choice != '8');
    return 0;
}