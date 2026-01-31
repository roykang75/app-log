from fastapi import FastAPI, Request
import logging
import json

app = FastAPI()

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

@app.post("/api/v1/fix")
async def trigger_fix(request: Request):
    """
    Receives Webhook from Grafana Alerting.
    Payload contains details about the alert.
    """
    try:
        payload = await request.json()
        logger.info(f"Received Webhook Payload: {json.dumps(payload, indent=2)}")
        
        # Simulate analyzing the alert payload
        alerts = payload.get('alerts', [])
        for alert in alerts:
            labels = alert.get('labels', {})
            logger.info(f"Analyzed Alert: {labels.get('alertname')} - Fix Required: {labels.get('is_fix_required')}")
            
            if labels.get('is_fix_required') == 'true':
                logger.info(">>> AUTOMATIC FIX INITIATED <<<")
                logger.info("Step 1: Analyzing stack trace...")
                logger.info("Step 2: Applying patch...")
                logger.info("Step 3: Restarting module...")
                logger.info(">>> FIX COMPLETED <<<")
            else:
                logger.info("No automatic fix required for this alert.")

        return {"status": "success", "message": "Webhook processed"}
    except Exception as e:
        logger.error(f"Error processing webhook: {str(e)}")
        return {"status": "error", "message": str(e)}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=5000)
