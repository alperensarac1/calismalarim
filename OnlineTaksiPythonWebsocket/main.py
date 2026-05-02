# app/main.py

from fastapi import FastAPI
from app.core.database import Base, engine

# Modeller
from app.models.user import User
from app.models.customer_profile import CustomerProfile
from app.models.driver_profile import DriverProfile
from app.models.ride_request import RideRequest
from app.models.ride_status_log import RideStatusLog

# REST routerlar
from app.api.auth import router as auth_router
from app.api.customer import router as customer_router
from app.api.driver import router as driver_router

# WebSocket router
from app.websocket.ws_routes import router as websocket_router

Base.metadata.create_all(bind=engine)

app = FastAPI(title="onlinetaksi Backend", version="1.0.0")

app.include_router(auth_router)
app.include_router(customer_router)
app.include_router(driver_router)
app.include_router(websocket_router)


@app.get("/")
def root():
    return {"message": "onlinetaksi backend ayakta"}


@app.get("/health")
def health():
    return {"status": "ok"}