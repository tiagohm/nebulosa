import { Injectable } from '@angular/core'
import moment from 'moment'
import { DARVStart, TPPAStart } from '../types/alignment.types'
import { Angle, BodyPosition, CloseApproach, ComputedLocation, Constellation, DeepSkyObject, MinorPlanet, Satellite, SatelliteGroupType, SkyObjectType, Twilight } from '../types/atlas.types'
import { AutoFocusRequest } from '../types/autofocus.type'
import { CalibrationFrame } from '../types/calibration.types'
import { Camera, CameraStartCapture } from '../types/camera.types'
import { Device, INDIProperty, INDISendProperty } from '../types/device.types'
import { FlatWizardRequest } from '../types/flat-wizard.types'
import { Focuser } from '../types/focuser.types'
import { HipsSurvey } from '../types/framing.types'
import { GuideDirection, GuideOutput, Guider, GuiderHistoryStep, SettleInfo } from '../types/guider.types'
import { ConnectionStatus, ConnectionType } from '../types/home.types'
import { CoordinateInterpolation, DetectedStar, FOVCamera, FOVTelescope, ImageAnnotation, ImageInfo, ImageSaveDialog, ImageSolved, ImageTransformation } from '../types/image.types'
import { CelestialLocationType, Mount, MountRemoteControl, MountRemoteControlType, SlewRate, TrackMode } from '../types/mount.types'
import { PlateSolverRequest } from '../types/platesolver.types'
import { Rotator } from '../types/rotator.types'
import { SequencePlan } from '../types/sequencer.types'
import { AnalyzedTarget, StackingRequest } from '../types/stacker.types'
import { StarDetectionRequest } from '../types/stardetector.types'
import { Wheel } from '../types/wheel.types'
import { Undefinable } from '../utils/types'
import { HttpService } from './http.service'

@Injectable({ providedIn: 'root' })
export class ApiService {
	constructor(private readonly http: HttpService) {}

	get baseUrl() {
		return this.http.baseUrl
	}

	// CONNECTION

	connect(host: string, port: number, type: ConnectionType) {
		const query = this.http.query({ host, port, type })
		return this.http.put<string>(`connection?${query}`)
	}

	disconnect(id: string) {
		return this.http.delete<never>(`connection/${id}`)
	}

	connectionStatuses() {
		return this.http.get<ConnectionStatus[]>(`connection`)
	}

	connectionStatus(id: string) {
		return this.http.get<Undefinable<ConnectionStatus>>(`connection/${id}`)
	}

	// CAMERA

	cameras() {
		return this.http.get<Camera[]>(`cameras`)
	}

	camera(id: string) {
		return this.http.get<Camera>(`cameras/${id}`)
	}

	cameraConnect(camera: Camera) {
		return this.http.put<never>(`cameras/${camera.id}/connect`)
	}

	cameraDisconnect(camera: Camera) {
		return this.http.put<never>(`cameras/${camera.id}/disconnect`)
	}

	cameraIsCapturing(camera: Camera) {
		return this.http.get<boolean>(`cameras/${camera.id}/capturing`)
	}

	cameraCooler(camera: Camera, enabled: boolean) {
		return this.http.put<never>(`cameras/${camera.id}/cooler?enabled=${enabled}`)
	}

	cameraSetpointTemperature(camera: Camera, temperature: number) {
		return this.http.put<never>(`cameras/${camera.id}/temperature/setpoint?temperature=${temperature}`)
	}

	cameraStartCapture(camera: Camera, data: CameraStartCapture, mount?: Mount, wheel?: Wheel, focuser?: Focuser, rotator?: Rotator) {
		const query = this.http.query({ mount: mount?.id, wheel: wheel?.id, focuser: focuser?.id, rotator: rotator?.id })
		return this.http.put<never>(`cameras/${camera.id}/capture/start?${query}`, data)
	}

	cameraPauseCapture(camera: Camera) {
		return this.http.put<never>(`cameras/${camera.id}/capture/pause`)
	}

	cameraUnpauseCapture(camera: Camera) {
		return this.http.put<never>(`cameras/${camera.id}/capture/unpause`)
	}

	cameraAbortCapture(camera: Camera) {
		return this.http.put<never>(`cameras/${camera.id}/capture/abort`)
	}

	cameraListen(camera: Camera) {
		return this.http.put<never>(`cameras/${camera.id}/listen`)
	}

	// MOUNT

	mounts() {
		return this.http.get<Mount[]>(`mounts`)
	}

	mount(id: string) {
		return this.http.get<Mount>(`mounts/${id}`)
	}

	mountConnect(mount: Mount) {
		return this.http.put<never>(`mounts/${mount.id}/connect`)
	}

	mountDisconnect(mount: Mount) {
		return this.http.put<never>(`mounts/${mount.id}/disconnect`)
	}

	mountTracking(mount: Mount, enabled: boolean) {
		return this.http.put<never>(`mounts/${mount.id}/tracking?enabled=${enabled}`)
	}

	mountSync(mount: Mount, rightAscension: Angle, declination: Angle, j2000: boolean) {
		const query = this.http.query({ rightAscension, declination, j2000 })
		return this.http.put<never>(`mounts/${mount.id}/sync?${query}`)
	}

	mountSlew(mount: Mount, rightAscension: Angle, declination: Angle, j2000: boolean) {
		const query = this.http.query({ rightAscension, declination, j2000, hasConfirmation: true })
		return this.http.put<never>(`mounts/${mount.id}/slew?${query}`)
	}

	mountGoTo(mount: Mount, rightAscension: Angle, declination: Angle, j2000: boolean) {
		const query = this.http.query({ rightAscension, declination, j2000, hasConfirmation: true })
		return this.http.put<never>(`mounts/${mount.id}/goto?${query}`)
	}

	mountPark(mount: Mount) {
		return this.http.put<never>(`mounts/${mount.id}/park`)
	}

	mountUnpark(mount: Mount) {
		return this.http.put<never>(`mounts/${mount.id}/unpark`)
	}

	mountHome(mount: Mount) {
		return this.http.put<never>(`mounts/${mount.id}/home`)
	}

	mountAbort(mount: Mount) {
		return this.http.put<never>(`mounts/${mount.id}/abort`)
	}

	mountTrackMode(mount: Mount, mode: TrackMode) {
		return this.http.put<never>(`mounts/${mount.id}/track-mode?mode=${mode}`)
	}

	mountSlewRate(mount: Mount, rate: SlewRate) {
		return this.http.put<never>(`mounts/${mount.id}/slew-rate?rate=${rate.name}`)
	}

	mountMove(mount: Mount, direction: GuideDirection, enabled: boolean) {
		return this.http.put<never>(`mounts/${mount.id}/move?direction=${direction}&enabled=${enabled}`)
	}

	mountComputeLocation(mount: Mount, j2000: boolean, rightAscension: Angle, declination: Angle, equatorial: boolean = true, horizontal: boolean = true, meridianAt: boolean = false) {
		const query = this.http.query({ rightAscension, declination, j2000, equatorial, horizontal, meridianAt })
		return this.http.get<ComputedLocation>(`mounts/${mount.id}/location?${query}`)
	}

	mountCelestialLocation(mount: Mount, type: CelestialLocationType) {
		return this.http.get<ComputedLocation>(`mounts/${mount.id}/location/${type}`)
	}

	pointMountHere(mount: Mount, path: string, x: number, y: number) {
		const query = this.http.query({ path, x, y })
		return this.http.put<never>(`mounts/${mount.id}/point-here?${query}`)
	}

	mountRemoteControlStart(mount: Mount, type: MountRemoteControlType, host: string, port: number) {
		const query = this.http.query({ type, host, port })
		return this.http.put<never>(`mounts/${mount.id}/remote-control/start?${query}`)
	}

	mountRemoteControlList(mount: Mount) {
		return this.http.get<MountRemoteControl[]>(`mounts/${mount.id}/remote-control`)
	}

	mountRemoteControlStop(mount: Mount, type: MountRemoteControlType) {
		const query = this.http.query({ type })
		return this.http.put<never>(`mounts/${mount.id}/remote-control/stop?${query}`)
	}

	mountListen(mount: Mount) {
		return this.http.put<never>(`mounts/${mount.id}/listen`)
	}

	// FOCUSER

	focusers() {
		return this.http.get<Focuser[]>(`focusers`)
	}

	focuser(id: string) {
		return this.http.get<Focuser>(`focusers/${id}`)
	}

	focuserConnect(focuser: Focuser) {
		return this.http.put<never>(`focusers/${focuser.id}/connect`)
	}

	focuserDisconnect(focuser: Focuser) {
		return this.http.put<never>(`focusers/${focuser.id}/disconnect`)
	}

	focuserMoveIn(focuser: Focuser, steps: number) {
		return this.http.put<never>(`focusers/${focuser.id}/move-in?steps=${steps}`)
	}

	focuserMoveOut(focuser: Focuser, steps: number) {
		return this.http.put<never>(`focusers/${focuser.id}/move-out?steps=${steps}`)
	}

	focuserMoveTo(focuser: Focuser, steps: number) {
		return this.http.put<never>(`focusers/${focuser.id}/move-to?steps=${steps}`)
	}

	focuserAbort(focuser: Focuser) {
		return this.http.put<never>(`focusers/${focuser.id}/abort`)
	}

	focuserSync(focuser: Focuser, steps: number) {
		return this.http.put<never>(`focusers/${focuser.id}/sync?steps=${steps}`)
	}

	focuserListen(focuser: Focuser) {
		return this.http.put<never>(`focusers/${focuser.id}/listen`)
	}

	// FILTER WHEEL

	wheels() {
		return this.http.get<Wheel[]>(`wheels`)
	}

	wheel(id: string) {
		return this.http.get<Wheel>(`wheels/${id}`)
	}

	wheelConnect(wheel: Wheel) {
		return this.http.put<never>(`wheels/${wheel.id}/connect`)
	}

	wheelDisconnect(wheel: Wheel) {
		return this.http.put<never>(`wheels/${wheel.id}/disconnect`)
	}

	wheelMoveTo(wheel: Wheel, position: number) {
		return this.http.put<never>(`wheels/${wheel.id}/move-to?position=${position}`)
	}

	wheelSync(wheel: Wheel, names: string[]) {
		return this.http.put<never>(`wheels/${wheel.id}/sync?names=${names.join(',')}`)
	}

	wheelListen(wheel: Wheel) {
		return this.http.put<never>(`wheels/${wheel.id}/listen`)
	}

	// ROTATOR

	rotators() {
		return this.http.get<Rotator[]>(`rotators`)
	}

	rotator(id: string) {
		return this.http.get<Rotator>(`rotators/${id}`)
	}

	rotatorConnect(rotator: Rotator) {
		return this.http.put<never>(`rotators/${rotator.id}/connect`)
	}

	rotatorDisconnect(rotator: Rotator) {
		return this.http.put<never>(`rotators/${rotator.id}/disconnect`)
	}

	rotatorReverse(rotator: Rotator, enabled: boolean) {
		return this.http.put<never>(`rotators/${rotator.id}/reverse?enabled=${enabled}`)
	}

	rotatorMove(rotator: Rotator, angle: number) {
		return this.http.put<never>(`rotators/${rotator.id}/move?angle=${angle}`)
	}

	rotatorAbort(rotator: Rotator) {
		return this.http.put<never>(`rotators/${rotator.id}/abort`)
	}

	rotatorHome(rotator: Rotator) {
		return this.http.put<never>(`rotators/${rotator.id}/home`)
	}

	rotatorSync(rotator: Rotator, angle: number) {
		return this.http.put<never>(`rotators/${rotator.id}/sync?angle=${angle}`)
	}

	rotatorListen(rotator: Rotator) {
		return this.http.put<never>(`rotators/${rotator.id}/listen`)
	}

	// GUIDE OUTPUT

	guideOutputs() {
		return this.http.get<GuideOutput[]>(`guide-outputs`)
	}

	guideOutput(id: string) {
		return this.http.get<GuideOutput>(`guide-outputs/${id}`)
	}

	guideOutputConnect(guideOutput: GuideOutput) {
		return this.http.put<never>(`guide-outputs/${guideOutput.id}/connect`)
	}

	guideOutputDisconnect(guideOutput: GuideOutput) {
		return this.http.put<never>(`guide-outputs/${guideOutput.id}/disconnect`)
	}

	guideOutputPulse(guideOutput: GuideOutput, direction: GuideDirection, duration: number) {
		const query = this.http.query({ direction, duration })
		return this.http.put<never>(`guide-outputs/${guideOutput.id}/pulse?${query}`)
	}

	guideOutputListen(guideOutput: GuideOutput) {
		return this.http.put<never>(`guide-outputs/${guideOutput.id}/listen`)
	}

	// GUIDING

	guidingConnect(host: string = 'localhost', port: number = 4400) {
		const query = this.http.query({ host, port })
		return this.http.put<never>(`guiding/connect?${query}`)
	}

	guidingDisconnect() {
		return this.http.delete<never>(`guiding/disconnect`)
	}

	guidingStatus() {
		return this.http.get<Guider>(`guiding/status`)
	}

	guidingHistory(maxLength: number = 100) {
		const query = this.http.query({ maxLength })
		return this.http.get<GuiderHistoryStep[]>(`guiding/history?${query}`)
	}

	guidingLatestHistory() {
		return this.http.get<GuiderHistoryStep | null>(`guiding/history/latest`)
	}

	guidingClearHistory() {
		return this.http.put<never>(`guiding/history/clear`)
	}

	guidingLoop(autoSelectGuideStar: boolean = true) {
		const query = this.http.query({ autoSelectGuideStar })
		return this.http.put<never>(`guiding/loop?${query}`)
	}

	guidingStart(forceCalibration: boolean = false) {
		const query = this.http.query({ forceCalibration })
		return this.http.put<never>(`guiding/start?${query}`)
	}

	guidingDither(amount: number, raOnly: boolean = false) {
		const query = this.http.query({ amount, raOnly })
		return this.http.put<never>(`guiding/dither?${query}`)
	}

	guidingSettle(settle: SettleInfo) {
		return this.http.put<never>(`guiding/settle`, settle)
	}

	guidingStop() {
		return this.http.put<never>(`guiding/stop`)
	}

	// IMAGE

	async openImage(path: string, transformation: ImageTransformation, camera?: Camera): Promise<{ blob: Blob | null; info?: ImageInfo }> {
		const query = this.http.query({ path, camera: camera?.id })
		const response = await this.http.postBlob(`image?${query}`, transformation)
		const header = response.headers.get('X-Image-Info')

		if (header) {
			const info = JSON.parse(header) as ImageInfo
			return { info, blob: response.body }
		} else {
			return { blob: response.body }
		}
	}

	closeImage(path: string) {
		const query = this.http.query({ path })
		return this.http.delete<never>(`image?${query}`)
	}

	// INDI

	indiDevice<T extends Device = Device>(device: T) {
		return this.http.get<T>(`indi/${device.id}`)
	}

	indiDeviceConnect(device: Device) {
		return this.http.put<never>(`indi/${device.id}/connect`)
	}

	indiDeviceDisconnect(device: Device) {
		return this.http.put<never>(`indi/${device.id}/disconnect`)
	}

	indiProperties(device: Device) {
		return this.http.get<INDIProperty[]>(`indi/${device.id}/properties`)
	}

	indiSendProperty(device: Device, property: INDISendProperty) {
		return this.http.put<never>(`indi/${device.id}/send`, property)
	}

	indiListen(device: Device) {
		return this.http.put<never>(`indi/${device.id}/listen`)
	}

	indiUnlisten(device: Device) {
		return this.http.put<never>(`indi/${device.id}/unlisten`)
	}

	indiLog(device: Device) {
		return this.http.get<string[]>(`indi/${device.id}/log`)
	}

	// SKY ATLAS

	positionOfSun(dateTime: Date, fast: boolean = false) {
		const [date, time] = moment(dateTime).format('YYYY-MM-DD HH:mm').split(' ')
		const query = this.http.query({ date, time, fast, hasLocation: true })
		return this.http.get<BodyPosition>(`sky-atlas/sun/position?${query}`)
	}

	altitudePointsOfSun(dateTime: Date, fast: boolean = false) {
		const date = moment(dateTime).format('YYYY-MM-DD')
		const query = this.http.query({ date, fast, hasLocation: true })
		return this.http.get<[number, number][]>(`sky-atlas/sun/altitude-points?${query}`)
	}

	positionOfMoon(dateTime: Date, fast: boolean = false) {
		const [date, time] = moment(dateTime).format('YYYY-MM-DD HH:mm').split(' ')
		const query = this.http.query({ date, time, fast, hasLocation: true })
		return this.http.get<BodyPosition>(`sky-atlas/moon/position?${query}`)
	}

	altitudePointsOfMoon(dateTime: Date, fast: boolean = false) {
		const date = moment(dateTime).format('YYYY-MM-DD')
		const query = this.http.query({ date, fast, hasLocation: true })
		return this.http.get<[number, number][]>(`sky-atlas/moon/altitude-points?${query}`)
	}

	positionOfPlanet(code: string, dateTime: Date, fast: boolean = false) {
		const [date, time] = moment(dateTime).format('YYYY-MM-DD HH:mm').split(' ')
		const query = this.http.query({ date, time, fast, hasLocation: true })
		return this.http.get<BodyPosition>(`sky-atlas/planets/${encodeURIComponent(code)}/position?${query}`)
	}

	altitudePointsOfPlanet(code: string, dateTime: Date, fast: boolean = false) {
		const date = moment(dateTime).format('YYYY-MM-DD')
		const query = this.http.query({ date, fast, hasLocation: true })
		return this.http.get<[number, number][]>(`sky-atlas/planets/${encodeURIComponent(code)}/altitude-points?${query}`)
	}

	positionOfSkyObject(simbad: DeepSkyObject, dateTime: Date) {
		const [date, time] = moment(dateTime).format('YYYY-MM-DD HH:mm').split(' ')
		const query = this.http.query({ date, time, hasLocation: true })
		return this.http.get<BodyPosition>(`sky-atlas/sky-objects/${simbad.id}/position?${query}`)
	}

	altitudePointsOfSkyObject(simbad: DeepSkyObject, dateTime: Date) {
		const date = moment(dateTime).format('YYYY-MM-DD')
		const query = this.http.query({ date, hasLocation: true })
		return this.http.get<[number, number][]>(`sky-atlas/sky-objects/${simbad.id}/altitude-points?${query}`)
	}

	searchSkyObject(text: string, rightAscension: Angle, declination: Angle, radius: Angle, constellation?: Constellation, magnitudeMin: number = -99, magnitudeMax: number = 99, type?: SkyObjectType) {
		const query = this.http.query({ text, rightAscension, declination, radius, constellation, magnitudeMin, magnitudeMax, type })
		return this.http.get<DeepSkyObject[]>(`sky-atlas/sky-objects?${query}`)
	}

	skyObjectTypes() {
		return this.http.get<SkyObjectType[]>(`sky-atlas/sky-objects/types`)
	}

	positionOfSatellite(satellite: Satellite, dateTime: Date) {
		const [date, time] = moment(dateTime).format('YYYY-MM-DD HH:mm').split(' ')
		const query = this.http.query({ date, time, hasLocation: true })
		return this.http.get<BodyPosition>(`sky-atlas/satellites/${satellite.id}/position?${query}`)
	}

	altitudePointsOfSatellite(satellite: Satellite, dateTime: Date) {
		const date = moment(dateTime).format('YYYY-MM-DD')
		const query = this.http.query({ date, hasLocation: true })
		return this.http.get<[number, number][]>(`sky-atlas/satellites/${satellite.id}/altitude-points?${query}`)
	}

	searchSatellites(text: string = '', group: SatelliteGroupType[] = []) {
		const query = this.http.query({ text, group })
		return this.http.get<Satellite[]>(`sky-atlas/satellites?${query}`)
	}

	twilight(dateTime: Date, fast: boolean = false) {
		const date = moment(dateTime).format('YYYY-MM-DD')
		const query = this.http.query({ date, fast, hasLocation: true })
		return this.http.get<Twilight>(`sky-atlas/twilight?${query}`)
	}

	searchMinorPlanet(text: string) {
		const query = this.http.query({ text })
		return this.http.get<MinorPlanet>(`sky-atlas/minor-planets?${query}`)
	}

	closeApproachesForMinorPlanets(days: number = 7, distance: number = 10, dateTime?: Date | string) {
		const date = !dateTime || typeof dateTime === 'string' ? dateTime : moment(dateTime).format('YYYY-MM-DD')
		const query = this.http.query({ days, distance, date })
		return this.http.get<CloseApproach[]>(`sky-atlas/minor-planets/close-approaches?${query}`)
	}

	annotationsOfImage(path: string, starsAndDSOs: boolean = true, minorPlanets: boolean = false, minorPlanetMagLimit: number = 12.0, includeMinorPlanetsWithoutMagnitude: boolean = false, useSimbad: boolean = false) {
		const query = this.http.query({ path, starsAndDSOs, minorPlanets, minorPlanetMagLimit, includeMinorPlanetsWithoutMagnitude, useSimbad, hasLocation: true })
		return this.http.get<ImageAnnotation[]>(`image/annotations?${query}`)
	}

	saveImageAs(path: string, save: ImageSaveDialog, camera?: Camera) {
		const query = this.http.query({ path, camera: camera?.id })
		return this.http.put<never>(`image/save-as?${query}`, save)
	}

	coordinateInterpolation(path: string) {
		const query = this.http.query({ path })
		return this.http.get<CoordinateInterpolation | null>(`image/coordinate-interpolation?${query}`)
	}

	detectStars(path: string, starDetector: StarDetectionRequest) {
		const query = this.http.query({ path })
		return this.http.put<DetectedStar[]>(`star-detection?${query}`, starDetector)
	}

	imageHistogram(path: string, bitLength: number = 16) {
		const query = this.http.query({ path, bitLength })
		return this.http.get<number[]>(`image/histogram?${query}`)
	}

	fovCameras() {
		return this.http.get<FOVCamera[]>('image/fov-cameras')
	}

	fovTelescopes() {
		return this.http.get<FOVTelescope[]>('image/fov-telescopes')
	}

	// CALIBRATION

	calibrationGroups() {
		return this.http.get<string[]>('calibration-frames')
	}

	calibrationFrames(name: string) {
		return this.http.get<CalibrationFrame[]>(`calibration-frames/${name}`)
	}

	uploadCalibrationFrame(name: string, path: string) {
		const query = this.http.query({ path })
		return this.http.put<CalibrationFrame[]>(`calibration-frames/${name}?${query}`)
	}

	updateCalibrationFrame(frame: CalibrationFrame) {
		return this.http.post<CalibrationFrame>('calibration-frames', frame)
	}

	deleteCalibrationFrame(frame: CalibrationFrame) {
		return this.http.delete<never>(`calibration-frames/${frame.id}`)
	}

	// FRAMING

	hipsSurveys() {
		return this.http.get<HipsSurvey[]>('framing/hips-surveys')
	}

	frame(rightAscension: Angle, declination: Angle, width: number, height: number, fov: number, rotation: number, hipsSurvey: HipsSurvey) {
		const query = this.http.query({ rightAscension, declination, width, height, fov, rotation, hipsSurvey: hipsSurvey.id })
		return this.http.put<string>(`framing?${query}`)
	}

	// DARV

	darvStart(camera: Camera, guideOutput: GuideOutput, data: DARVStart) {
		return this.http.put<never>(`polar-alignment/darv/${camera.id}/${guideOutput.id}/start`, data)
	}

	darvStop(camera: Camera) {
		return this.http.put<never>(`polar-alignment/darv/${camera.id}/stop`)
	}

	// TPPA

	tppaStart(camera: Camera, mount: Mount, data: TPPAStart) {
		return this.http.put<never>(`polar-alignment/tppa/${camera.id}/${mount.id}/start`, data)
	}

	tppaStop(camera: Camera) {
		return this.http.put<never>(`polar-alignment/tppa/${camera.id}/stop`)
	}

	tppaPause(camera: Camera) {
		return this.http.put<never>(`polar-alignment/tppa/${camera.id}/pause`)
	}

	tppaUnpause(camera: Camera) {
		return this.http.put<never>(`polar-alignment/tppa/${camera.id}/unpause`)
	}

	// SEQUENCER

	sequencerStart(camera: Camera, plan: SequencePlan) {
		const body: SequencePlan = { ...plan, mount: undefined, camera: undefined, wheel: undefined, focuser: undefined }
		const query = this.http.query({ mount: plan.mount, focuser: plan.focuser, wheel: plan.wheel })
		return this.http.put<never>(`sequencer/${camera.id}/start?${query}`, body)
	}

	sequencerPause(camera: Camera) {
		return this.http.put<never>(`sequencer/${camera.id}/pause`)
	}

	sequencerUnpause(camera: Camera) {
		return this.http.put<never>(`sequencer/${camera.id}/unpause`)
	}

	sequencerStop(camera: Camera) {
		return this.http.put<never>(`sequencer/${camera.id}/stop`)
	}

	// FLAT WIZARD

	flatWizardStart(camera: Camera, request: FlatWizardRequest) {
		return this.http.put<never>(`flat-wizard/${camera.id}/start`, request)
	}

	flatWizardStop(camera: Camera) {
		return this.http.put<never>(`flat-wizard/${camera.id}/stop`)
	}

	// SOLVER

	solverStart(solver: PlateSolverRequest, path: string, blind: boolean, centerRA: Angle, centerDEC: Angle, radius: Angle) {
		const query = this.http.query({ path, blind, centerRA, centerDEC, radius })
		return this.http.put<ImageSolved>(`plate-solver/start?${query}`, solver)
	}

	solverStop() {
		return this.http.put<never>('plate-solver/stop')
	}

	// AUTO FOCUS

	autoFocusStart(camera: Camera, focuser: Focuser, request: AutoFocusRequest) {
		return this.http.put<never>(`auto-focus/${camera.id}/${focuser.id}/start`, request)
	}

	autoFocusStop(camera: Camera) {
		return this.http.put<never>(`auto-focus/${camera.id}/stop`)
	}

	// STACKER

	stackerStart(request: StackingRequest) {
		return this.http.put<string | null>('stacker/start', request)
	}

	stackerIsRunning() {
		return this.http.get<boolean>('stacker/running')
	}

	stackerStop() {
		return this.http.put<never>('stacker/stop')
	}

	stackerAnalyze(path: string) {
		return this.http.put<AnalyzedTarget | null>(`stacker/analyze?path=${path}`)
	}

	// CONFIRMATION

	confirm(idempotencyKey: string, accepted: boolean) {
		const query = this.http.query({ accepted })
		return this.http.put(`confirmation/${idempotencyKey}?${query}`)
	}
}
