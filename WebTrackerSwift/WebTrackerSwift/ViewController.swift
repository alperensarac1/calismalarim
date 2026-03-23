import UIKit
import WebKit

final class ViewController: UIViewController {

    @IBOutlet weak var tfUrl: UITextField!
    @IBOutlet weak var btnLoad: UIButton!
    @IBOutlet weak var btnCopyAll: UIButton!

    @IBOutlet weak var swEnableFilter: UISwitch!
    @IBOutlet weak var swOnlyApi: UISwitch!
    @IBOutlet weak var swEnableJsHook: UISwitch!
    @IBOutlet weak var swOnlyGet: UISwitch!
    @IBOutlet weak var swOnlyPost: UISwitch!

    @IBOutlet weak var tfSearch: UITextField!
    @IBOutlet weak var webContainerView: UIView!
    @IBOutlet weak var tableViewLogs: UITableView!

    private var webView: WKWebView!
    private var messageHandler: WebViewMessageHandler!

    private var allLogs: [NetworkLog] = []
    private var visibleLogs: [NetworkLog] = []
    private var seenRequests: Set<String> = []

    private var filterOptions = FilterOptions()

    override func viewDidLoad() {
        super.viewDidLoad()

        setupUI()
        setupTableView()
        setupWebView()
        setupActions()
        applyDefaults()
    }

    private func setupUI() {
        tfUrl.borderStyle = .roundedRect
        tfSearch.borderStyle = .roundedRect

        tfUrl.delegate = self
        tfSearch.delegate = self
    }

    private func setupTableView() {
        tableViewLogs.dataSource = self
        tableViewLogs.delegate = self
        tableViewLogs.register(UITableViewCell.self, forCellReuseIdentifier: "LogCell")
        tableViewLogs.rowHeight = UITableView.automaticDimension
        tableViewLogs.estimatedRowHeight = 100
    }

    private func setupWebView() {
        messageHandler = WebViewMessageHandler { [weak self] json in
            guard let self else { return }
            if !self.filterOptions.enableJsHook { return }
            self.parseAndAddJsLog(json)
        }

        let contentController = WKUserContentController()
        contentController.add(messageHandler, name: "iosLogger")

        let js = """
        (function() {
            if (window.__ALPEREN_HOOK_INSTALLED__) return;
            window.__ALPEREN_HOOK_INSTALLED__ = true;

            function safeStringify(value) {
                try {
                    if (typeof value === 'string') return value;
                    return JSON.stringify(value);
                } catch (e) {
                    return '[stringify_error]';
                }
            }

            function notifyiOS(data) {
                try {
                    window.webkit.messageHandlers.iosLogger.postMessage(JSON.stringify(data));
                } catch (e) {}
            }

            const originalFetch = window.fetch;
            window.fetch = function(input, init) {
                let url = '';
                let method = 'GET';
                let body = null;

                try {
                    if (typeof input === 'string') {
                        url = input;
                    } else if (input && input.url) {
                        url = input.url;
                    }

                    if (init && init.method) {
                        method = init.method;
                    } else if (input && input.method) {
                        method = input.method;
                    }

                    if (init && init.body !== undefined) {
                        body = safeStringify(init.body);
                    }
                } catch (e) {}

                notifyiOS({
                    source: 'JS_HOOK',
                    type: 'fetch',
                    url: url,
                    method: method,
                    body: body
                });

                return originalFetch.apply(this, arguments);
            };

            const originalOpen = XMLHttpRequest.prototype.open;
            const originalSend = XMLHttpRequest.prototype.send;

            XMLHttpRequest.prototype.open = function(method, url) {
                this.__req_method = method || 'GET';
                this.__req_url = url || '';
                return originalOpen.apply(this, arguments);
            };

            XMLHttpRequest.prototype.send = function(body) {
                notifyiOS({
                    source: 'JS_HOOK',
                    type: 'xhr',
                    url: this.__req_url || '',
                    method: this.__req_method || 'GET',
                    body: safeStringify(body)
                });
                return originalSend.apply(this, arguments);
            };
        })();
        """

        let userScript = WKUserScript(
            source: js,
            injectionTime: .atDocumentEnd,
            forMainFrameOnly: false
        )
        contentController.addUserScript(userScript)

        let config = WKWebViewConfiguration()
        config.userContentController = contentController

        webView = WKWebView(frame: .zero, configuration: config)
        webView.navigationDelegate = self
        webView.translatesAutoresizingMaskIntoConstraints = false

        webContainerView.addSubview(webView)

        NSLayoutConstraint.activate([
            webView.topAnchor.constraint(equalTo: webContainerView.topAnchor),
            webView.bottomAnchor.constraint(equalTo: webContainerView.bottomAnchor),
            webView.leadingAnchor.constraint(equalTo: webContainerView.leadingAnchor),
            webView.trailingAnchor.constraint(equalTo: webContainerView.trailingAnchor)
        ])
    }

    private func setupActions() {
        btnLoad.addTarget(self, action: #selector(loadTapped), for: .touchUpInside)
        btnCopyAll.addTarget(self, action: #selector(copyAllTapped), for: .touchUpInside)

        swEnableFilter.addTarget(self, action: #selector(filtersChanged), for: .valueChanged)
        swOnlyApi.addTarget(self, action: #selector(filtersChanged), for: .valueChanged)
        swEnableJsHook.addTarget(self, action: #selector(filtersChanged), for: .valueChanged)
        swOnlyGet.addTarget(self, action: #selector(onlyGetChanged), for: .valueChanged)
        swOnlyPost.addTarget(self, action: #selector(onlyPostChanged), for: .valueChanged)

        tfSearch.addTarget(self, action: #selector(searchChanged), for: .editingChanged)
    }

    private func applyDefaults() {
        tfUrl.text = "https://example.com"

        swEnableFilter.isOn = true
        swOnlyApi.isOn = false
        swEnableJsHook.isOn = true
        swOnlyGet.isOn = false
        swOnlyPost.isOn = false

        syncFilterOptionsFromUI()
    }

    @objc private func loadTapped() {
        view.endEditing(true)

        guard let input = tfUrl.text?.trimmingCharacters(in: .whitespacesAndNewlines), !input.isEmpty else {
            return
        }

        let finalUrl = normalizeUrl(input)

        tfUrl.text = finalUrl
        allLogs.removeAll()
        visibleLogs.removeAll()
        seenRequests.removeAll()
        tableViewLogs.reloadData()

        guard let url = URL(string: finalUrl) else { return }
        webView.load(URLRequest(url: url))
    }

    @objc private func copyAllTapped() {
        UIPasteboard.general.string = buildAllLogsText()
        showAlert(title: "Bilgi", message: "Tüm istekler panoya kopyalandı.")
    }

    @objc private func filtersChanged() {
        syncFilterOptionsFromUI()
        refreshVisibleLogs()
    }

    @objc private func onlyGetChanged() {
        if swOnlyGet.isOn {
            swOnlyPost.setOn(false, animated: true)
        }
        syncFilterOptionsFromUI()
        refreshVisibleLogs()
    }

    @objc private func onlyPostChanged() {
        if swOnlyPost.isOn {
            swOnlyGet.setOn(false, animated: true)
        }
        syncFilterOptionsFromUI()
        refreshVisibleLogs()
    }

    @objc private func searchChanged() {
        syncFilterOptionsFromUI()
        refreshVisibleLogs()
    }

    private func syncFilterOptionsFromUI() {
        filterOptions.enableFilter = swEnableFilter.isOn
        filterOptions.onlyApiRequests = swOnlyApi.isOn
        filterOptions.enableJsHook = swEnableJsHook.isOn
        filterOptions.showOnlyGet = swOnlyGet.isOn
        filterOptions.showOnlyPost = swOnlyPost.isOn
        filterOptions.searchQuery = tfSearch.text ?? ""
    }

    private func normalizeUrl(_ value: String) -> String {
        if value.hasPrefix("http://") || value.hasPrefix("https://") {
            return value
        }
        return "https://\(value)"
    }

    private func addLogIfNeeded(_ log: NetworkLog) {
        let key = "\(log.source)_\(log.method)_\(log.url)_\(log.requestBody ?? "")_\(log.time)"
        guard !seenRequests.contains(key) else { return }

        seenRequests.insert(key)
        allLogs.insert(log, at: 0)
        refreshVisibleLogs()
    }

    private func refreshVisibleLogs() {
        let filters = filterOptions

        visibleLogs = allLogs.filter { log in
            let searchOk: Bool
            if filters.searchQuery.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                searchOk = true
            } else {
                searchOk = log.url.localizedCaseInsensitiveContains(filters.searchQuery)
            }

            if !filters.enableFilter {
                return searchOk
            }

            let methodOk: Bool
            if filters.showOnlyGet {
                methodOk = log.method.caseInsensitiveCompare("GET") == .orderedSame
            } else if filters.showOnlyPost {
                methodOk = log.method.caseInsensitiveCompare("POST") == .orderedSame
            } else {
                methodOk =
                    log.method.caseInsensitiveCompare("GET") == .orderedSame ||
                    log.method.caseInsensitiveCompare("POST") == .orderedSame
            }

            let ignoredOk = !RequestUtils.shouldIgnoreUrl(log.url)

            let apiOk: Bool
            if filters.onlyApiRequests {
                apiOk =
                    RequestUtils.looksLikeApi(log.url) ||
                    log.resourceType.caseInsensitiveCompare("api") == .orderedSame ||
                    log.source.caseInsensitiveCompare("JS_HOOK") == .orderedSame
            } else {
                apiOk = true
            }

            return searchOk && methodOk && ignoredOk && apiOk
        }

        tableViewLogs.reloadData()
    }

    private func parseAndAddJsLog(_ jsonString: String) {
        guard
            let data = jsonString.data(using: .utf8),
            let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any]
        else {
            return
        }

        let url = json["url"] as? String ?? ""
        let method = json["method"] as? String ?? "GET"
        let body = json["body"] as? String
        let source = json["source"] as? String ?? "JS_HOOK"
        let host = URL(string: url)?.host ?? "Bilinmiyor"

        let log = NetworkLog(
            method: method,
            url: url,
            host: host,
            time: RequestUtils.currentTimeString(),
            headers: [:],
            isMainFrame: false,
            resourceType: "api",
            requestBody: body,
            source: source
        )

        addLogIfNeeded(log)
    }

    private func showLogDetail(_ log: NetworkLog) {
        let text = formatSingleLog(log)

        let alert = UIAlertController(title: "İstek Detayı", message: text, preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "Kopyala", style: .default, handler: { _ in
            UIPasteboard.general.string = text
        }))
        alert.addAction(UIAlertAction(title: "Replay", style: .default, handler: { _ in
            self.showReplayDialog(for: log)
        }))
        alert.addAction(UIAlertAction(title: "Kapat", style: .cancel))
        present(alert, animated: true)
    }

    private func showReplayDialog(for log: NetworkLog) {
        let initialBaseUrl: String
        let initialParams: String

        if log.method.caseInsensitiveCompare("GET") == .orderedSame {
            let split = RequestUtils.splitUrlAndQuery(log.url)
            initialBaseUrl = split.0
            initialParams = split.1
        } else {
            initialBaseUrl = log.url
            initialParams = log.requestBody ?? ""
        }

        let alert = UIAlertController(title: "Replay", message: "Base URL ve Query / Body düzenle", preferredStyle: .alert)

        alert.addTextField { tf in
            tf.placeholder = "Base URL"
            tf.text = initialBaseUrl
        }

        alert.addTextField { tf in
            tf.placeholder = "Query / Body"
            tf.text = initialParams
        }

        alert.addAction(UIAlertAction(title: "Sadece Test Et", style: .default, handler: { _ in
            let baseUrl = alert.textFields?[0].text ?? ""
            let params = alert.textFields?[1].text ?? ""

            ReplayService.replay(originalLog: log, editedBaseUrl: baseUrl, editedParams: params) { result in
                self.showAlert(title: "Replay Sonucu", message: result)
            }
        }))

        alert.addAction(UIAlertAction(title: "WebView'de Aç + Test Et", style: .default, handler: { _ in
            let baseUrl = alert.textFields?[0].text ?? ""
            let params = alert.textFields?[1].text ?? ""

            self.openReplayInWebView(originalLog: log, editedBaseUrl: baseUrl, editedParams: params)

            ReplayService.replay(originalLog: log, editedBaseUrl: baseUrl, editedParams: params) { result in
                self.showAlert(title: "Replay Sonucu", message: result)
            }
        }))

        alert.addAction(UIAlertAction(title: "Kapat", style: .cancel))
        present(alert, animated: true)
    }

    private func openReplayInWebView(
        originalLog: NetworkLog,
        editedBaseUrl: String,
        editedParams: String
    ) {
        if originalLog.method.caseInsensitiveCompare("GET") == .orderedSame {
            let finalUrl = RequestUtils.buildFinalUrl(baseUrl: editedBaseUrl, query: editedParams)
            tfUrl.text = finalUrl

            if let url = URL(string: finalUrl) {
                webView.load(URLRequest(url: url))
            }
        } else if originalLog.method.caseInsensitiveCompare("POST") == .orderedSame {
            let contentType = RequestUtils.detectContentType(for: editedParams)

            if contentType.hasPrefix("application/x-www-form-urlencoded"),
               let url = URL(string: editedBaseUrl),
               let bodyData = editedParams.data(using: .utf8) {
                var request = URLRequest(url: url)
                request.httpMethod = "POST"
                request.httpBody = bodyData
                request.setValue(contentType, forHTTPHeaderField: "Content-Type")

                tfUrl.text = editedBaseUrl
                webView.load(request)
            } else {
                showAlert(title: "Bilgi", message: "JSON/plain POST gövdesi WebView içinde her zaman uygun çalışmayabilir.")
            }
        }
    }

    private func formatSingleLog(_ log: NetworkLog) -> String {
        var text = ""
        text += "========================================\n"
        text += "METHOD      : \(log.method)\n"
        text += "SOURCE      : \(log.source)\n"
        text += "TYPE        : \(log.resourceType)\n"
        text += "TIME        : \(log.time)\n"
        text += "HOST        : \(log.host)\n"
        text += "MAIN_FRAME  : \(log.isMainFrame)\n"
        text += "URL         : \(log.url)\n"
        text += "HEADERS     :\n"

        if log.headers.isEmpty {
            text += "  - yok\n"
        } else {
            for (key, value) in log.headers {
                text += "  \(key): \(value)\n"
            }
        }

        text += "BODY        :\n"
        text += log.requestBody ?? "yok"
        return text
    }

    private func buildAllLogsText() -> String {
        if allLogs.isEmpty {
            return "Henüz kopyalanacak istek yok."
        }

        var result = "TOPLAM ISTEK SAYISI: \(allLogs.count)\n\n"
        for (index, log) in allLogs.enumerated() {
            result += "ISTEK #\(index + 1)\n"
            result += formatSingleLog(log)
            result += "\n\n"
        }
        return result
    }

    private func showAlert(title: String, message: String) {
        let alert = UIAlertController(
            title: title,
            message: message.count > 5000 ? String(message.prefix(5000)) : message,
            preferredStyle: .alert
        )
        alert.addAction(UIAlertAction(title: "Tamam", style: .default))
        present(alert, animated: true)
    }
}

extension ViewController: UITableViewDataSource, UITableViewDelegate {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        visibleLogs.count
    }

    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let log = visibleLogs[indexPath.row]
        tableView.deselectRow(at: indexPath, animated: true)
        showLogDetail(log)
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {

        let log = visibleLogs[indexPath.row]

        let cell = tableView.dequeueReusableCell(withIdentifier: "LogCell", for: indexPath)
        var content = cell.defaultContentConfiguration()

        let preview = log.requestBody.map { String($0.prefix(80)) } ?? "yok"

        content.text = "\(log.method) | \(log.source)"
        content.secondaryText = """
        URL: \(log.url)
        Host: \(log.host)
        Zaman: \(log.time)
        Body: \(preview)
        """

        cell.contentConfiguration = content
        return cell
    }
}

extension ViewController: UITextFieldDelegate {
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        if textField == tfUrl {
            loadTapped()
        } else {
            textField.resignFirstResponder()
        }
        return true
    }
}

extension ViewController: WKNavigationDelegate {
    func webView(_ webView: WKWebView, decidePolicyFor navigationAction: WKNavigationAction, decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {

        let request = navigationAction.request
        let method = request.httpMethod ?? "GET"
        let url = request.url?.absoluteString ?? ""
        let host = request.url?.host ?? "Bilinmiyor"
        let headers = request.allHTTPHeaderFields ?? [:]
        let body: String? = {
            guard let data = request.httpBody else { return nil }
            return String(data: data, encoding: .utf8)
        }()

        let log = NetworkLog(
            method: method,
            url: url,
            host: host,
            time: RequestUtils.currentTimeString(),
            headers: headers,
            isMainFrame: navigationAction.targetFrame?.isMainFrame ?? false,
            resourceType: RequestUtils.guessResourceType(url),
            requestBody: body,
            source: "WEBVIEW"
        )

        addLogIfNeeded(log)
        decisionHandler(.allow)
    }
}
