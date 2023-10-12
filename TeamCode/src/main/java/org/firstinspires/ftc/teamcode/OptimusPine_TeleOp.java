import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name="OptimusPine_TeleOp", group="Linear OpMode")

public class OptimusPine_TeleOp extends LinearOpMode {
    private DcMotor leftMotor;
    private DcMotor rightMotor;
    private DcMotor armMotor;
    private Servo leftClaw;
    private Servo rightClaw;

    private boolean clawOpen = true; // Initial state of the claw
    private boolean armModeHold = false; // Flag to indicate whether the arm motor is in hold mode

    @Override
    public void runOpMode() {
        // Initialize motors
        leftMotor = hardwareMap.get(DcMotor.class, "left_motor");
        rightMotor = hardwareMap.get(DcMotor.class, "right_motor");
        armMotor = hardwareMap.get(DcMotor.class, "arm_motor");
        
        // Initialize servos
        leftClaw = hardwareMap.get(Servo.class, "left_claw");
        rightClaw = hardwareMap.get(Servo.class, "right_claw");

        // Reverse the right motor so that both motors move in the same direction
        rightMotor.setDirection(DcMotor.Direction.REVERSE);

        // Reverse the left claw servo direction
        leftClaw.setDirection(Servo.Direction.REVERSE);

        // Reset the arm motor encoder
        armMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        armMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        waitForStart();

        double leftPower;
        double rightPower;

        while (opModeIsActive()) {
            // Read joystick values
            double drive = -gamepad2.left_stick_y;
            double turn = gamepad2.left_stick_x;
            double armPower = gamepad2.right_stick_y;

            // Limit the range of values using Range.clip
            leftPower = Range.clip(drive + turn, -1.0, 1.0);
            rightPower = Range.clip(drive - turn, -1.0, 1.0);

            // Check if the right stick is not moved (in a neutral position)
            if (Math.abs(armPower) > 0.05) {
                // Set the arm motor's power
                armMotor.setPower(0.3 * Range.clip(armPower, -1.0, 1.0)); // Adjust the scaling factor as needed
                armModeHold = false; // Arm motor is not in hold mode
            } else {
                // Stop the arm motor and put it in hold mode
                if (!armModeHold) {
                    armMotor.setPower(0.0);
                    armMotor.setTargetPosition(armMotor.getCurrentPosition()); // Hold current position
                    armMotor.setPower(0.1); // Set a small power to hold position
                    armMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION); // Put motor in hold mode
                    armModeHold = true; // Arm motor is in hold mode
                }
            }

            // Set motor powers
            leftMotor.setPower(leftPower);
            rightMotor.setPower(rightPower);

            // Toggle the claw open/close when the A button is pressed
            if (gamepad2.a) {
                if (clawOpen) {
                    leftClaw.setPosition(Range.clip(0.5, 0.0, 1.0)); // Use the original value for the left claw
                    rightClaw.setPosition(Range.clip(0.75, 0.0, 1.0)); // Adjust this value to close the right claw less
                } else {
                    leftClaw.setPosition(Range.clip(0.0, 0.0, 1.0)); // Use the original value for the left claw
                    rightClaw.setPosition(Range.clip(0.5, 0.0, 1.0)); // Adjust this value to open the right claw less
                }
                clawOpen = !clawOpen; // Toggle the claw state
                sleep(500); // Delay to prevent rapid toggling
            }

            idle();
        }
    }
}
