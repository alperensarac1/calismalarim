import sys
import os
import threading
import time
import webbrowser
from pathlib import Path


def resource_path(relative_path):
    """
    Normal çalışmada proje klasörünü,
    PyInstaller portable klasör modunda _internal içeriğini kullanır.
    """

    if hasattr(sys, "_MEIPASS"):
        base_path = Path(sys._MEIPASS)
    else:
        base_path = Path(__file__).resolve().parent

    return base_path / relative_path


def open_browser_later():
    time.sleep(6)
    webbrowser.open("http://127.0.0.1:8501")


def main():
    app_path = resource_path("app.py")
    config_path = resource_path(".streamlit/config.toml")

    if not app_path.exists():
        raise FileNotFoundError(f"app.py bulunamadı: {app_path}")

    os.environ["STREAMLIT_GLOBAL_DEVELOPMENT_MODE"] = "false"

    os.environ["STREAMLIT_SERVER_HEADLESS"] = "true"
    os.environ["STREAMLIT_SERVER_ADDRESS"] = "127.0.0.1"
    os.environ["STREAMLIT_SERVER_PORT"] = "8501"
    os.environ["STREAMLIT_SERVER_ENABLE_CORS"] = "false"
    os.environ["STREAMLIT_SERVER_ENABLE_XSRF_PROTECTION"] = "false"

    os.environ["STREAMLIT_BROWSER_SERVER_ADDRESS"] = "127.0.0.1"
    os.environ["STREAMLIT_BROWSER_SERVER_PORT"] = "8501"
    os.environ["STREAMLIT_BROWSER_GATHER_USAGE_STATS"] = "false"

    if config_path.exists():
        os.environ["STREAMLIT_CONFIG_FILE"] = str(config_path)

    threading.Thread(
        target=open_browser_later,
        daemon=True
    ).start()

    from streamlit.web import bootstrap

    flag_options = {
        "global.developmentMode": False,
        "server.headless": True,
        "server.address": "127.0.0.1",
        "server.port": 8501,
        "server.enableCORS": False,
        "server.enableXsrfProtection": False,
        "browser.serverAddress": "127.0.0.1",
        "browser.serverPort": 8501,
        "browser.gatherUsageStats": False,
    }

    bootstrap.run(
        str(app_path),
        "",
        [],
        flag_options=flag_options
    )


if __name__ == "__main__":
    main()