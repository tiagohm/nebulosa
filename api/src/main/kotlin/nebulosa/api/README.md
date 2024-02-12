# Nebulosa API

## Web Socket Events

URL: `localhost:{PORT}/ws`

### Camera

#### CAMERA.UPDATED, CAMERA.ATTACHED, CAMERA.DETACHED

```json5
{
    "device": {
        "exposuring": false,
        "hasCoolerControl": false,
        "coolerPower": 0,
        "cooler": false,
        "hasDewHeater": false,
        "dewHeater": false,
        "frameFormats": [],
        "canAbort": false,
        "cfaOffsetX": 0,
        "cfaOffsetY": 0,
        "cfaType": "RGGB",
        "exposureMin": 0,
        "exposureMax": 1,
        "exposureState": "IDLE",
        "exposureTime": 1,
        "hasCooler": false,
        "canSetTemperature": false,
        "canSubFrame": false,
        "x": 0,
        "minX": 0,
        "maxX": 0,
        "y": 0,
        "minY": 0,
        "maxY": 0,
        "width": 1023,
        "minWidth": 1023,
        "maxWidth": 1023,
        "height": 1280,
        "minHeight": 1280,
        "maxHeight": 1280,
        "canBin": false,
        "maxBinX": 1,
        "maxBinY": 1,
        "binX": 1,
        "binY": 1,
        "gain": 0,
        "gainMin": 0,
        "gainMax": 0,
        "offset": 0,
        "offsetMin": 0,
        "offsetMax": 0,
        "hasGuiderHead": false,
        "pixelSizeX": 0,
        "pixelSizeY": 0,
        "capturesPath": "",
        "canPulseGuide": false,
        "pulseGuiding": false,
        "name": "",
        "connected": false,
        "hasThermometer": false,
        "temperature": 0
    }
}
```

#### CAMERA.CAPTURE_ELAPSED

```json5
{
    "camera": {},
    "state": "CAPTURE_STARTED|EXPOSURE_STARTED|EXPOSURING|WAITING|SETTLING|EXPOSURE_FINISHED|CAPTURE_FINISHED",
    "exposureAmount": 0,
    "exposureCount": 0,
    "captureElapsedTime": 0,
    "captureProgress": 0.0,
    "captureRemainingTime": 0,
    "exposureProgress": 0,
    "exposureRemainingTime": 0.0,
    "waitRemainingTime": 0,
    "waitProgress": 0.0,
    "savePath": "",
}
```

### Mount

#### MOUNT.UPDATED, MOUNT.ATTACHED, MOUNT.DETACHED

```json5
{
    "device": {
        "slewing": false,
        "tracking": false,
        "canAbort": false,
        "canSync": false,
        "canGoTo": false,
        "canHome": false,
        "slewRates": [],
        "trackModes": [],
        "trackMode": "SIDEREAL",
        "pierSide": "NEITHER",
        "guideRateWE": 0,
        "guideRateNS": 0,
        "rightAscension": "00h00m00s",
        "declination": "00°00\"00",
        "hasGPS": false,
        "longitude": 0,
        "latitude": 0,
        "elevation": 0,
        "dateTime": 0,
        "offsetInMinutes": 0,
        "name": "",
        "connected": false,
        "canPulseGuide": false,
        "pulseGuiding": false,
        "canPark": false,
        "parking": false,
        "parked": false
    }
}
```

### Filter Wheel

#### WHEEL.UPDATED, WHEEL.ATTACHED, WHEEL.DETACHED

```json5
{
    "device": {
        "count": 0,
        "position": 0,
        "moving": false,
        "name": "",
        "connected": false
    }
}
```

### Focuser

#### FOCUSER.UPDATED, FOCUSER.ATTACHED, FOCUSER.DETACHED

```json5
{
    "device": {
        "moving": false,
        "position": 0,
        "canAbsoluteMove": false,
        "canRelativeMove": false,
        "canAbort": false,
        "canReverse": false,
        "reverse": false,
        "canSync": false,
        "hasBacklash": false,
        "maxPosition": 0,
        "name": "",
        "connected": false,
        "hasThermometer": false,
        "temperature": 0
    }
}
```

### Guide Output

#### GUIDE_OUTPUT.UPDATED, GUIDE_OUTPUT.ATTACHED, GUIDE_OUTPUT.DETACHED

```json5
{
    "device": {
        "canPulseGuide": false,
        "pulseGuiding": false,
        "name": "",
        "connected": false
    }
}
```

### DARV Polar Alignment

#### DARV.ELAPSED

```json5
{
    "camera": {},
    "guideOutput": {},
    "remainingTime": 0,
    "progress": 0.0,
    "direction": "EAST",
    "state": "FORWARD|BACKWARD"
}
```

### Three Point Polar Alignment

#### TPPA.ELAPSED

```json5
{
    "camera": {},
    "mount": {},
    "elapsedTime": 0,
    "stepCount": 0,
    "state": "SLEWING|SOLVING|SOLVED|COMPUTED|FAILED|FINISHED",
    "rightAscension": "00h00m00s",
    "declination": "00d00m00s",
    "azimuthError": "00d00m00s",
    "altitudeError": "00d00m00s",
    "totalError": "00d00m00s",
    "azimuthErrorDirection": "",
    "altitudeErrorDirection": ""
}
```

### Flat Wizard

#### FLAT_WIZARD.ELAPSED

```json5
{
    "state": "EXPOSURING|CAPTURED|FAILED",
    "exposureTime": 0,
    "savedPath": "",
    // CAMERA.CAPTURE_ELAPSED
    "capture": {},
    "message": ""
}
```

### Sequencer

#### SEQUENCER.ELAPSED

```json5
{
    "id": 0,
    "elapsedTime": 0,
    "remainingTime": 0,
    "progress": 0.0,
    // CAMERA.CAPTURE_ELAPSED
    "capture": {}
}
```

### INDI

#### DEVICE.PROPERTY_CHANGED, DEVICE.PROPERTY_DELETED

```json5
{
    "device": {},
    "property": {}
}
```

#### DEVICE.MESSAGE_RECEIVED

```json5
{
    "device": {},
    "message": ""
}
```
