import uvicorn
from constants.system_constants import *
from app import app

if __name__ == "__main__":
    uvicorn.run(app, host=IP, port=PORT)
