#!/usr/bin/env python3
import json
import os
import sys
import urllib.error
import urllib.parse
import urllib.request
from datetime import datetime, timedelta


BASE_URL = os.environ.get("NHNKCP_BASE_URL", "http://localhost:8080")
LOG_PATH = os.environ.get("NHNKCP_LOG_PATH", "api_calls.log")
LOG_FILE = None
HEADERS = {"Content-Type": "application/json"}


def log_line(message):
    if LOG_FILE is None:
        raise RuntimeError("LOG_FILE is not initialized")
    LOG_FILE.write(message + "\n")
    LOG_FILE.flush()


def build_url(path, params=None):
    if params:
        query = urllib.parse.urlencode(params)
        return f"{BASE_URL}{path}?{query}"
    return f"{BASE_URL}{path}"


def build_curl(method, url, payload=None):
    parts = [f"CURL -X{method}", f"'{url}'"]
    if "Content-Type" in HEADERS:
        parts.append(f"-H 'Content-Type: {HEADERS['Content-Type']}'")
    if payload is not None:
        body = json.dumps(payload, ensure_ascii=True)
        parts.append(f"-d '{body}'")
    return " ".join(parts)


def http_request(method, path, payload=None, params=None):
    url = build_url(path, params)
    log_line(build_curl(method, url, payload))
    data = None
    if payload is not None:
        data = json.dumps(payload).encode("utf-8")
    request = urllib.request.Request(url, data=data, headers=HEADERS, method=method)
    try:
        with urllib.request.urlopen(request) as response:
            body = response.read().decode("utf-8")
            log_line(f"Response: {body}")
            return response.status, body
    except urllib.error.HTTPError as exc:
        body = exc.read().decode("utf-8")
        log_line(f"Response: {body}")
        return exc.code, body


def parse_json(body):
    if not body:
        return None
    try:
        return json.loads(body)
    except json.JSONDecodeError:
        return None


def get_created_at(order_data):
    if not order_data:
        return None
    created_at = order_data.get("createdAt")
    if not created_at:
        return None
    try:
        return datetime.fromisoformat(created_at)
    except ValueError:
        if "." in created_at:
            base, frac = created_at.split(".", 1)
            frac = (frac + "000000")[:6]
            try:
                return datetime.fromisoformat(f"{base}.{frac}")
            except ValueError:
                return None
        return None


def main():
    log_dir = os.path.dirname(LOG_PATH)
    if log_dir:
        os.makedirs(log_dir, exist_ok=True)

    global LOG_FILE
    LOG_FILE = open(LOG_PATH, "w", encoding="utf-8")
    try:
        log_line(f"Base URL: {BASE_URL}")

        product_ids = []
        order_id = None
        created_at = None

        # 1) Create products
        payload = {
            "name": "cold brew",
            "price": 4500,
            "stockQuantity": 10,
            "category": "BEVERAGE",
        }
        _, body = http_request("POST", "/api/products", payload)
        resp = parse_json(body)
        product = resp.get("data") if isinstance(resp, dict) else None
        if product and product.get("id"):
            product_ids.append(product["id"])

        payload = {
            "name": "choco cookie",
            "price": 2500,
            "stockQuantity": 20,
            "category": "FOOD",
        }
        _, body = http_request("POST", "/api/products", payload)
        resp = parse_json(body)
        product = resp.get("data") if isinstance(resp, dict) else None
        if product and product.get("id"):
            product_ids.append(product["id"])

        # 2) List products (all / category)
        http_request("GET", "/api/products")
        http_request("GET", "/api/products", params={"category": "FOOD"})

        # 3) Get product
        if product_ids:
            http_request("GET", f"/api/products/{product_ids[0]}")

        # 4) Update product
        if product_ids:
            payload = {
                "name": "cold brew (renewal)",
                "price": 4800,
                "stockQuantity": 8,
                "category": "BEVERAGE",
            }
            http_request("PUT", f"/api/products/{product_ids[0]}", payload)

        # 5) Create order
        if product_ids:
            payload = {
                "items": [
                    {"productId": product_ids[0], "quantity": 2},
                    {"productId": product_ids[1], "quantity": 1},
                ]
            }
            _, body = http_request("POST", "/api/orders", payload)
            resp = parse_json(body)
            order = resp.get("data") if isinstance(resp, dict) else None
            if order and order.get("id"):
                order_id = order["id"]
            created_at = get_created_at(order)
            for product_id in product_ids:
                http_request("GET", f"/api/products/{product_id}")

        # 6) List orders (all)
        http_request("GET", "/api/orders")

        # 7) Update order status
        if order_id is not None:
            payload = {"status": "RECEIVED"}
            http_request("PATCH", f"/api/orders/{order_id}/status", payload)
            for product_id in product_ids:
                http_request("GET", f"/api/products/{product_id}")

        if order_id is not None:
            payload = {"status": "COMPLETED"}
            http_request("PATCH", f"/api/orders/{order_id}/status", payload)
            for product_id in product_ids:
                http_request("GET", f"/api/products/{product_id}")

        # 7-1) Cancel order (after completed)
        if order_id is not None:
            payload = {"status": "CANCELED"}
            http_request("PATCH", f"/api/orders/{order_id}/status", payload)
            for product_id in product_ids:
                http_request("GET", f"/api/products/{product_id}")

        # 8) Get order
        if order_id is not None:
            http_request("GET", f"/api/orders/{order_id}")

        # 9) List orders (status)
        http_request("GET", "/api/orders", params={"status": "COMPLETED"})

        # 10) List orders (period)
        if created_at is not None:
            start = (created_at - timedelta(minutes=1)).isoformat()
            end = (created_at + timedelta(minutes=1)).isoformat()
            http_request("GET", "/api/orders", params={"start": start, "end": end})

        log_line("All API calls completed.")
    finally:
        LOG_FILE.close()


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        sys.exit(130)
