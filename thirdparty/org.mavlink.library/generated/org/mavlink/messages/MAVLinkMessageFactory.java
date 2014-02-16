/**
 * Generated class : MAVLinkMessageFactory
 * DO NOT MODIFY!
 **/
package org.mavlink.messages;
import org.mavlink.messages.MAVLinkMessage;
import org.mavlink.IMAVLinkMessage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.mavlink.messages.ardupilotmega.msg_optical_flow;
import org.mavlink.messages.ardupilotmega.msg_hil_gps;
import org.mavlink.messages.ardupilotmega.msg_omnidirectional_flow;
import org.mavlink.messages.ardupilotmega.msg_safety_set_allowed_area;
import org.mavlink.messages.ardupilotmega.msg_mission_request_partial_list;
import org.mavlink.messages.ardupilotmega.msg_command_long;
import org.mavlink.messages.ardupilotmega.msg_manual_control;
import org.mavlink.messages.ardupilotmega.msg_local_position_ned;
import org.mavlink.messages.ardupilotmega.msg_attitude;
import org.mavlink.messages.ardupilotmega.msg_battery_status;
import org.mavlink.messages.ardupilotmega.msg_global_position_int;
import org.mavlink.messages.ardupilotmega.msg_mission_request;
import org.mavlink.messages.ardupilotmega.msg_mission_ack;
import org.mavlink.messages.ardupilotmega.msg_debug;
import org.mavlink.messages.ardupilotmega.msg_setpoint_8dof;
import org.mavlink.messages.ardupilotmega.msg_nav_controller_output;
import org.mavlink.messages.ardupilotmega.msg_heartbeat;
import org.mavlink.messages.ardupilotmega.msg_param_request_read;
import org.mavlink.messages.ardupilotmega.msg_setpoint_6dof;
import org.mavlink.messages.ardupilotmega.msg_attitude_quaternion;
import org.mavlink.messages.ardupilotmega.msg_param_set;
import org.mavlink.messages.ardupilotmega.msg_file_transfer_dir_list;
import org.mavlink.messages.ardupilotmega.msg_highres_imu;
import org.mavlink.messages.ardupilotmega.msg_set_quad_swarm_led_roll_pitch_yaw_thrust;
import org.mavlink.messages.ardupilotmega.msg_scaled_imu;
import org.mavlink.messages.ardupilotmega.msg_rc_channels_raw;
import org.mavlink.messages.ardupilotmega.msg_global_position_setpoint_int;
import org.mavlink.messages.ardupilotmega.msg_radio_status;
import org.mavlink.messages.ardupilotmega.msg_hil_rc_inputs_raw;
import org.mavlink.messages.ardupilotmega.msg_set_global_position_setpoint_int;
import org.mavlink.messages.ardupilotmega.msg_param_request_list;
import org.mavlink.messages.ardupilotmega.msg_mission_clear_all;
import org.mavlink.messages.ardupilotmega.msg_change_operator_control_ack;
import org.mavlink.messages.ardupilotmega.msg_param_value;
import org.mavlink.messages.ardupilotmega.msg_roll_pitch_yaw_speed_thrust_setpoint;
import org.mavlink.messages.ardupilotmega.msg_hil_controls;
import org.mavlink.messages.ardupilotmega.msg_mission_item;
import org.mavlink.messages.ardupilotmega.msg_local_position_ned_system_global_offset;
import org.mavlink.messages.ardupilotmega.msg_named_value_float;
import org.mavlink.messages.ardupilotmega.msg_gps_raw_int;
import org.mavlink.messages.ardupilotmega.msg_local_position_setpoint;
import org.mavlink.messages.ardupilotmega.msg_set_gps_global_origin;
import org.mavlink.messages.ardupilotmega.msg_roll_pitch_yaw_rates_thrust_setpoint;
import org.mavlink.messages.ardupilotmega.msg_set_quad_motors_setpoint;
import org.mavlink.messages.ardupilotmega.msg_set_roll_pitch_yaw_speed_thrust;
import org.mavlink.messages.ardupilotmega.msg_vision_speed_estimate;
import org.mavlink.messages.ardupilotmega.msg_servo_output_raw;
import org.mavlink.messages.ardupilotmega.msg_scaled_pressure;
import org.mavlink.messages.ardupilotmega.msg_statustext;
import org.mavlink.messages.ardupilotmega.msg_hil_sensor;
import org.mavlink.messages.ardupilotmega.msg_manual_setpoint;
import org.mavlink.messages.ardupilotmega.msg_vfr_hud;
import org.mavlink.messages.ardupilotmega.msg_sys_status;
import org.mavlink.messages.ardupilotmega.msg_vision_position_estimate;
import org.mavlink.messages.ardupilotmega.msg_debug_vect;
import org.mavlink.messages.ardupilotmega.msg_system_time;
import org.mavlink.messages.ardupilotmega.msg_hil_state_quaternion;
import org.mavlink.messages.ardupilotmega.msg_change_operator_control;
import org.mavlink.messages.ardupilotmega.msg_hil_optical_flow;
import org.mavlink.messages.ardupilotmega.msg_mission_write_partial_list;
import org.mavlink.messages.ardupilotmega.msg_raw_pressure;
import org.mavlink.messages.ardupilotmega.msg_gps_global_origin;
import org.mavlink.messages.ardupilotmega.msg_safety_allowed_area;
import org.mavlink.messages.ardupilotmega.msg_mission_current;
import org.mavlink.messages.ardupilotmega.msg_set_mode;
import org.mavlink.messages.ardupilotmega.msg_ping;
import org.mavlink.messages.ardupilotmega.msg_mission_item_reached;
import org.mavlink.messages.ardupilotmega.msg_rc_channels_scaled;
import org.mavlink.messages.ardupilotmega.msg_file_transfer_res;
import org.mavlink.messages.ardupilotmega.msg_rc_channels_override;
import org.mavlink.messages.ardupilotmega.msg_set_roll_pitch_yaw_thrust;
import org.mavlink.messages.ardupilotmega.msg_mission_count;
import org.mavlink.messages.ardupilotmega.msg_hil_state;
import org.mavlink.messages.ardupilotmega.msg_set_local_position_setpoint;
import org.mavlink.messages.ardupilotmega.msg_command_ack;
import org.mavlink.messages.ardupilotmega.msg_data_stream;
import org.mavlink.messages.ardupilotmega.msg_gps_status;
import org.mavlink.messages.ardupilotmega.msg_set_quad_swarm_roll_pitch_yaw_thrust;
import org.mavlink.messages.ardupilotmega.msg_mission_request_list;
import org.mavlink.messages.ardupilotmega.msg_file_transfer_start;
import org.mavlink.messages.ardupilotmega.msg_mission_set_current;
import org.mavlink.messages.ardupilotmega.msg_raw_imu;
import org.mavlink.messages.ardupilotmega.msg_auth_key;
import org.mavlink.messages.ardupilotmega.msg_memory_vect;
import org.mavlink.messages.ardupilotmega.msg_sim_state;
import org.mavlink.messages.ardupilotmega.msg_named_value_int;
import org.mavlink.messages.ardupilotmega.msg_request_data_stream;
import org.mavlink.messages.ardupilotmega.msg_roll_pitch_yaw_thrust_setpoint;
import org.mavlink.messages.ardupilotmega.msg_vicon_position_estimate;
import org.mavlink.messages.ardupilotmega.msg_state_correction;
import org.mavlink.messages.ardupilotmega.msg_global_vision_position_estimate;
import org.mavlink.messages.ardupilotmega.msg_fence_fetch_point;
import org.mavlink.messages.ardupilotmega.msg_hil_gps;
import org.mavlink.messages.ardupilotmega.msg_radio;
import org.mavlink.messages.ardupilotmega.msg_safety_set_allowed_area;
import org.mavlink.messages.ardupilotmega.msg_local_position_ned;
import org.mavlink.messages.ardupilotmega.msg_attitude;
import org.mavlink.messages.ardupilotmega.msg_battery_status;
import org.mavlink.messages.ardupilotmega.msg_data32;
import org.mavlink.messages.ardupilotmega.msg_mission_request;
import org.mavlink.messages.ardupilotmega.msg_heartbeat;
import org.mavlink.messages.ardupilotmega.msg_nav_controller_output;
import org.mavlink.messages.ardupilotmega.msg_setpoint_8dof;
import org.mavlink.messages.ardupilotmega.msg_param_set;
import org.mavlink.messages.ardupilotmega.msg_file_transfer_dir_list;
import org.mavlink.messages.ardupilotmega.msg_set_quad_swarm_led_roll_pitch_yaw_thrust;
import org.mavlink.messages.ardupilotmega.msg_highres_imu;
import org.mavlink.messages.ardupilotmega.msg_meminfo;
import org.mavlink.messages.ardupilotmega.msg_scaled_imu;
import org.mavlink.messages.ardupilotmega.msg_rc_channels_raw;
import org.mavlink.messages.ardupilotmega.msg_radio_status;
import org.mavlink.messages.ardupilotmega.msg_param_request_list;
import org.mavlink.messages.ardupilotmega.msg_change_operator_control_ack;
import org.mavlink.messages.ardupilotmega.msg_param_value;
import org.mavlink.messages.ardupilotmega.msg_hil_controls;
import org.mavlink.messages.ardupilotmega.msg_local_position_ned_system_global_offset;
import org.mavlink.messages.ardupilotmega.msg_gps_raw_int;
import org.mavlink.messages.ardupilotmega.msg_set_quad_motors_setpoint;
import org.mavlink.messages.ardupilotmega.msg_servo_output_raw;
import org.mavlink.messages.ardupilotmega.msg_scaled_pressure;
import org.mavlink.messages.ardupilotmega.msg_manual_setpoint;
import org.mavlink.messages.ardupilotmega.msg_digicam_control;
import org.mavlink.messages.ardupilotmega.msg_debug_vect;
import org.mavlink.messages.ardupilotmega.msg_change_operator_control;
import org.mavlink.messages.ardupilotmega.msg_ahrs;
import org.mavlink.messages.ardupilotmega.msg_mission_write_partial_list;
import org.mavlink.messages.ardupilotmega.msg_safety_allowed_area;
import org.mavlink.messages.ardupilotmega.msg_gps_global_origin;
import org.mavlink.messages.ardupilotmega.msg_raw_pressure;
import org.mavlink.messages.ardupilotmega.msg_set_mag_offsets;
import org.mavlink.messages.ardupilotmega.msg_set_mode;
import org.mavlink.messages.ardupilotmega.msg_limits_status;
import org.mavlink.messages.ardupilotmega.msg_mission_item_reached;
import org.mavlink.messages.ardupilotmega.msg_ping;
import org.mavlink.messages.ardupilotmega.msg_rc_channels_scaled;
import org.mavlink.messages.ardupilotmega.msg_rc_channels_override;
import org.mavlink.messages.ardupilotmega.msg_file_transfer_res;
import org.mavlink.messages.ardupilotmega.msg_mission_request_list;
import org.mavlink.messages.ardupilotmega.msg_file_transfer_start;
import org.mavlink.messages.ardupilotmega.msg_data64;
import org.mavlink.messages.ardupilotmega.msg_mission_set_current;
import org.mavlink.messages.ardupilotmega.msg_raw_imu;
import org.mavlink.messages.ardupilotmega.msg_sim_state;
import org.mavlink.messages.ardupilotmega.msg_named_value_int;
import org.mavlink.messages.ardupilotmega.msg_state_correction;
import org.mavlink.messages.ardupilotmega.msg_wind;
import org.mavlink.messages.ardupilotmega.msg_global_vision_position_estimate;
import org.mavlink.messages.ardupilotmega.msg_optical_flow;
import org.mavlink.messages.ardupilotmega.msg_omnidirectional_flow;
import org.mavlink.messages.ardupilotmega.msg_mission_request_partial_list;
import org.mavlink.messages.ardupilotmega.msg_command_long;
import org.mavlink.messages.ardupilotmega.msg_ap_adc;
import org.mavlink.messages.ardupilotmega.msg_manual_control;
import org.mavlink.messages.ardupilotmega.msg_fence_status;
import org.mavlink.messages.ardupilotmega.msg_rangefinder;
import org.mavlink.messages.ardupilotmega.msg_simstate;
import org.mavlink.messages.ardupilotmega.msg_mount_status;
import org.mavlink.messages.ardupilotmega.msg_mount_control;
import org.mavlink.messages.ardupilotmega.msg_global_position_int;
import org.mavlink.messages.ardupilotmega.msg_fence_point;
import org.mavlink.messages.ardupilotmega.msg_mission_ack;
import org.mavlink.messages.ardupilotmega.msg_debug;
import org.mavlink.messages.ardupilotmega.msg_param_request_read;
import org.mavlink.messages.ardupilotmega.msg_data96;
import org.mavlink.messages.ardupilotmega.msg_attitude_quaternion;
import org.mavlink.messages.ardupilotmega.msg_setpoint_6dof;
import org.mavlink.messages.ardupilotmega.msg_global_position_setpoint_int;
import org.mavlink.messages.ardupilotmega.msg_digicam_configure;
import org.mavlink.messages.ardupilotmega.msg_mount_configure;
import org.mavlink.messages.ardupilotmega.msg_hil_rc_inputs_raw;
import org.mavlink.messages.ardupilotmega.msg_set_global_position_setpoint_int;
import org.mavlink.messages.ardupilotmega.msg_mission_clear_all;
import org.mavlink.messages.ardupilotmega.msg_roll_pitch_yaw_speed_thrust_setpoint;
import org.mavlink.messages.ardupilotmega.msg_mission_item;
import org.mavlink.messages.ardupilotmega.msg_named_value_float;
import org.mavlink.messages.ardupilotmega.msg_local_position_setpoint;
import org.mavlink.messages.ardupilotmega.msg_hwstatus;
import org.mavlink.messages.ardupilotmega.msg_set_gps_global_origin;
import org.mavlink.messages.ardupilotmega.msg_roll_pitch_yaw_rates_thrust_setpoint;
import org.mavlink.messages.ardupilotmega.msg_set_roll_pitch_yaw_speed_thrust;
import org.mavlink.messages.ardupilotmega.msg_vision_speed_estimate;
import org.mavlink.messages.ardupilotmega.msg_hil_sensor;
import org.mavlink.messages.ardupilotmega.msg_statustext;
import org.mavlink.messages.ardupilotmega.msg_sys_status;
import org.mavlink.messages.ardupilotmega.msg_vfr_hud;
import org.mavlink.messages.ardupilotmega.msg_vision_position_estimate;
import org.mavlink.messages.ardupilotmega.msg_system_time;
import org.mavlink.messages.ardupilotmega.msg_rally_point;
import org.mavlink.messages.ardupilotmega.msg_hil_state_quaternion;
import org.mavlink.messages.ardupilotmega.msg_hil_optical_flow;
import org.mavlink.messages.ardupilotmega.msg_data16;
import org.mavlink.messages.ardupilotmega.msg_mission_current;
import org.mavlink.messages.ardupilotmega.msg_set_roll_pitch_yaw_thrust;
import org.mavlink.messages.ardupilotmega.msg_mission_count;
import org.mavlink.messages.ardupilotmega.msg_set_local_position_setpoint;
import org.mavlink.messages.ardupilotmega.msg_hil_state;
import org.mavlink.messages.ardupilotmega.msg_rally_fetch_point;
import org.mavlink.messages.ardupilotmega.msg_gps_status;
import org.mavlink.messages.ardupilotmega.msg_data_stream;
import org.mavlink.messages.ardupilotmega.msg_command_ack;
import org.mavlink.messages.ardupilotmega.msg_sensor_offsets;
import org.mavlink.messages.ardupilotmega.msg_set_quad_swarm_roll_pitch_yaw_thrust;
import org.mavlink.messages.ardupilotmega.msg_auth_key;
import org.mavlink.messages.ardupilotmega.msg_memory_vect;
import org.mavlink.messages.ardupilotmega.msg_roll_pitch_yaw_thrust_setpoint;
import org.mavlink.messages.ardupilotmega.msg_request_data_stream;
import org.mavlink.messages.ardupilotmega.msg_vicon_position_estimate;
import org.mavlink.messages.ardupilotmega.msg_airspeed_autocal;
/**
 * Class MAVLinkMessageFactory
 * Generate MAVLink message classes from byte array
 **/
public class MAVLinkMessageFactory implements IMAVLinkMessage, IMAVLinkMessageID {
public static MAVLinkMessage getMessage(int msgid, int sysId, int componentId, byte[] rawData) throws IOException {
    MAVLinkMessage msg=null;
    ByteBuffer dis = ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN);
    switch(msgid) {
  case MAVLINK_MSG_ID_FENCE_FETCH_POINT:
      msg = new msg_fence_fetch_point(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_HIL_GPS:
      msg = new msg_hil_gps(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_RADIO:
      msg = new msg_radio(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_SAFETY_SET_ALLOWED_AREA:
      msg = new msg_safety_set_allowed_area(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_LOCAL_POSITION_NED:
      msg = new msg_local_position_ned(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_ATTITUDE:
      msg = new msg_attitude(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_BATTERY_STATUS:
      msg = new msg_battery_status(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_DATA32:
      msg = new msg_data32(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_MISSION_REQUEST:
      msg = new msg_mission_request(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_HEARTBEAT:
      msg = new msg_heartbeat(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_NAV_CONTROLLER_OUTPUT:
      msg = new msg_nav_controller_output(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_SETPOINT_8DOF:
      msg = new msg_setpoint_8dof(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_PARAM_SET:
      msg = new msg_param_set(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_FILE_TRANSFER_DIR_LIST:
      msg = new msg_file_transfer_dir_list(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_SET_QUAD_SWARM_LED_ROLL_PITCH_YAW_THRUST:
      msg = new msg_set_quad_swarm_led_roll_pitch_yaw_thrust(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_HIGHRES_IMU:
      msg = new msg_highres_imu(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_MEMINFO:
      msg = new msg_meminfo(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_SCALED_IMU:
      msg = new msg_scaled_imu(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_RC_CHANNELS_RAW:
      msg = new msg_rc_channels_raw(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_RADIO_STATUS:
      msg = new msg_radio_status(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_PARAM_REQUEST_LIST:
      msg = new msg_param_request_list(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_CHANGE_OPERATOR_CONTROL_ACK:
      msg = new msg_change_operator_control_ack(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_PARAM_VALUE:
      msg = new msg_param_value(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_HIL_CONTROLS:
      msg = new msg_hil_controls(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_LOCAL_POSITION_NED_SYSTEM_GLOBAL_OFFSET:
      msg = new msg_local_position_ned_system_global_offset(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_GPS_RAW_INT:
      msg = new msg_gps_raw_int(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_SET_QUAD_MOTORS_SETPOINT:
      msg = new msg_set_quad_motors_setpoint(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_SERVO_OUTPUT_RAW:
      msg = new msg_servo_output_raw(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_SCALED_PRESSURE:
      msg = new msg_scaled_pressure(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_MANUAL_SETPOINT:
      msg = new msg_manual_setpoint(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_DIGICAM_CONTROL:
      msg = new msg_digicam_control(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_DEBUG_VECT:
      msg = new msg_debug_vect(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_CHANGE_OPERATOR_CONTROL:
      msg = new msg_change_operator_control(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_AHRS:
      msg = new msg_ahrs(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_MISSION_WRITE_PARTIAL_LIST:
      msg = new msg_mission_write_partial_list(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_SAFETY_ALLOWED_AREA:
      msg = new msg_safety_allowed_area(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_GPS_GLOBAL_ORIGIN:
      msg = new msg_gps_global_origin(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_RAW_PRESSURE:
      msg = new msg_raw_pressure(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_SET_MAG_OFFSETS:
      msg = new msg_set_mag_offsets(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_SET_MODE:
      msg = new msg_set_mode(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_LIMITS_STATUS:
      msg = new msg_limits_status(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_MISSION_ITEM_REACHED:
      msg = new msg_mission_item_reached(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_PING:
      msg = new msg_ping(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_RC_CHANNELS_SCALED:
      msg = new msg_rc_channels_scaled(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_RC_CHANNELS_OVERRIDE:
      msg = new msg_rc_channels_override(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_FILE_TRANSFER_RES:
      msg = new msg_file_transfer_res(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_MISSION_REQUEST_LIST:
      msg = new msg_mission_request_list(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_FILE_TRANSFER_START:
      msg = new msg_file_transfer_start(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_DATA64:
      msg = new msg_data64(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_MISSION_SET_CURRENT:
      msg = new msg_mission_set_current(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_RAW_IMU:
      msg = new msg_raw_imu(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_SIM_STATE:
      msg = new msg_sim_state(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_NAMED_VALUE_INT:
      msg = new msg_named_value_int(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_STATE_CORRECTION:
      msg = new msg_state_correction(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_WIND:
      msg = new msg_wind(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_GLOBAL_VISION_POSITION_ESTIMATE:
      msg = new msg_global_vision_position_estimate(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_OPTICAL_FLOW:
      msg = new msg_optical_flow(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_OMNIDIRECTIONAL_FLOW:
      msg = new msg_omnidirectional_flow(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_MISSION_REQUEST_PARTIAL_LIST:
      msg = new msg_mission_request_partial_list(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_COMMAND_LONG:
      msg = new msg_command_long(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_AP_ADC:
      msg = new msg_ap_adc(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_MANUAL_CONTROL:
      msg = new msg_manual_control(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_FENCE_STATUS:
      msg = new msg_fence_status(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_RANGEFINDER:
      msg = new msg_rangefinder(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_SIMSTATE:
      msg = new msg_simstate(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_MOUNT_STATUS:
      msg = new msg_mount_status(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_MOUNT_CONTROL:
      msg = new msg_mount_control(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_GLOBAL_POSITION_INT:
      msg = new msg_global_position_int(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_FENCE_POINT:
      msg = new msg_fence_point(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_MISSION_ACK:
      msg = new msg_mission_ack(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_DEBUG:
      msg = new msg_debug(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_PARAM_REQUEST_READ:
      msg = new msg_param_request_read(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_DATA96:
      msg = new msg_data96(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_ATTITUDE_QUATERNION:
      msg = new msg_attitude_quaternion(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_SETPOINT_6DOF:
      msg = new msg_setpoint_6dof(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_GLOBAL_POSITION_SETPOINT_INT:
      msg = new msg_global_position_setpoint_int(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_DIGICAM_CONFIGURE:
      msg = new msg_digicam_configure(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_MOUNT_CONFIGURE:
      msg = new msg_mount_configure(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_HIL_RC_INPUTS_RAW:
      msg = new msg_hil_rc_inputs_raw(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_SET_GLOBAL_POSITION_SETPOINT_INT:
      msg = new msg_set_global_position_setpoint_int(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_MISSION_CLEAR_ALL:
      msg = new msg_mission_clear_all(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_ROLL_PITCH_YAW_SPEED_THRUST_SETPOINT:
      msg = new msg_roll_pitch_yaw_speed_thrust_setpoint(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_MISSION_ITEM:
      msg = new msg_mission_item(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_NAMED_VALUE_FLOAT:
      msg = new msg_named_value_float(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_LOCAL_POSITION_SETPOINT:
      msg = new msg_local_position_setpoint(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_HWSTATUS:
      msg = new msg_hwstatus(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_SET_GPS_GLOBAL_ORIGIN:
      msg = new msg_set_gps_global_origin(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_ROLL_PITCH_YAW_RATES_THRUST_SETPOINT:
      msg = new msg_roll_pitch_yaw_rates_thrust_setpoint(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_SET_ROLL_PITCH_YAW_SPEED_THRUST:
      msg = new msg_set_roll_pitch_yaw_speed_thrust(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_VISION_SPEED_ESTIMATE:
      msg = new msg_vision_speed_estimate(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_HIL_SENSOR:
      msg = new msg_hil_sensor(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_STATUSTEXT:
      msg = new msg_statustext(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_SYS_STATUS:
      msg = new msg_sys_status(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_VFR_HUD:
      msg = new msg_vfr_hud(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_VISION_POSITION_ESTIMATE:
      msg = new msg_vision_position_estimate(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_SYSTEM_TIME:
      msg = new msg_system_time(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_RALLY_POINT:
      msg = new msg_rally_point(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_HIL_STATE_QUATERNION:
      msg = new msg_hil_state_quaternion(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_HIL_OPTICAL_FLOW:
      msg = new msg_hil_optical_flow(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_DATA16:
      msg = new msg_data16(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_MISSION_CURRENT:
      msg = new msg_mission_current(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_SET_ROLL_PITCH_YAW_THRUST:
      msg = new msg_set_roll_pitch_yaw_thrust(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_MISSION_COUNT:
      msg = new msg_mission_count(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_SET_LOCAL_POSITION_SETPOINT:
      msg = new msg_set_local_position_setpoint(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_HIL_STATE:
      msg = new msg_hil_state(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_RALLY_FETCH_POINT:
      msg = new msg_rally_fetch_point(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_GPS_STATUS:
      msg = new msg_gps_status(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_DATA_STREAM:
      msg = new msg_data_stream(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_COMMAND_ACK:
      msg = new msg_command_ack(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_SENSOR_OFFSETS:
      msg = new msg_sensor_offsets(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_SET_QUAD_SWARM_ROLL_PITCH_YAW_THRUST:
      msg = new msg_set_quad_swarm_roll_pitch_yaw_thrust(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_AUTH_KEY:
      msg = new msg_auth_key(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_MEMORY_VECT:
      msg = new msg_memory_vect(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_ROLL_PITCH_YAW_THRUST_SETPOINT:
      msg = new msg_roll_pitch_yaw_thrust_setpoint(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_REQUEST_DATA_STREAM:
      msg = new msg_request_data_stream(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_VICON_POSITION_ESTIMATE:
      msg = new msg_vicon_position_estimate(sysId, componentId);
      msg.decode(dis);
      break;
  case MAVLINK_MSG_ID_AIRSPEED_AUTOCAL:
      msg = new msg_airspeed_autocal(sysId, componentId);
      msg.decode(dis);
      break;
  default:
      System.out.println("Mavlink Factory Error : unknown MsgId : " + msgid);
    }
    return msg;
  }
}
