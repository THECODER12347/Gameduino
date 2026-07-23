package Code_Generation;

public final class AssemblyCodeSourceHandler {
	private static final String specAssemblyText = """
.include "m328Pdef.inc"

""";

    private static String commonResourcesAssemblyText= """
.equ SCK = 5
.equ MOSI = 3
.equ SS = 2

.equ RS_PIN = 1
.equ E_PIN = 0
.def MILLISECOND_COUNTERL = r18
.def MILLISECOND_COUNTERH = r19


.dseg
parameters:
	.byte 32
drawList:
	.byte 32
strMut:
	.byte 34
numberList:
	.byte 32
decList:
	.byte 64

.cseg
.org 0x0000
	rjmp entryInit
	
.org 0x0034
	AnimationFrames:
		.db 0x00 %s
.org 0x00B4
	Strings:
		.db 0b00101001, 0x59, 0x6F, 0x75, 0x20, 0x6C, 0x6F, 0x73, 0x65, 0x21
.org 0x0135

drawListZeroOut:
	push r16
	push r17
	push XL
	push XH

	ldi XL, low(drawList)
	ldi XH, high(drawList)
	ldi r17, 32
	ldi r16,0x00
	loopThroughDrawList:
		st X+, r16
		dec r17
		brne loopThroughDrawList
	ldi XL, 0x00
	ldi XH, 0x00
	ldi r17, 0x00
	ldi r16, 0x00
	
	pop XH
	pop XL
	pop r17
	pop r16

	ret


clearNumList:
	push r16
	push r17
	push XL
	push XH
	
	ldi XL, low(numberList)
	ldi XH, high(numberList)

	ldi r16, 0x00
	ldi r17, 32
	clearNums:
		st X+, r16
		dec r17
		brne clearNums
	
	pop XH
	pop XL
	pop r17
	pop r16
	ret

clearDecList:
	push r16
	push r17
	push XL
	push XH
	
	ldi XL, low(decList)
	ldi XH, high(decList)

	ldi r16, 0x00
	ldi r17, 64
	clearDec:
		st X+, r16
		dec r17
		brne clearDec
	
	pop XH
	pop XL
	pop r17
	pop r16
	ret

; 1-Sec delay for testing

delay_1s:
    push r20
    ldi r20, 200
	rjmp d1_loop

	d1_loop:
		rcall delay_5ms
		dec r20
		brne d1_loop

	    pop r20
		ret

; DELAYS FOR COMMUNICATION SUBROUTINES

delay_40us:
    push r18
	ldi r18, 160        ; 160 × 4 cycles ˜ 640 cycles
	d40_loop:
		nop                 ; 1 cycle
		dec r18             ; 1 cycle
		brne d40_loop       ; 2 cycles (taken)
		pop r18
		ret

delay_5ms:
	push r18
	push r19
    ldi r18, 50         ; outer loop
	d5_outer:
		ldi r19, 200        ; inner loop
		d5_inner:
			dec r19             ; 1 cycle
			brne d5_inner       ; 2 cycles
			dec r18
			brne d5_outer
			pop r19
			pop r18
			ret

delay_15ms:
    rcall delay_5ms
    rcall delay_5ms
    rcall delay_5ms
    ret
""";

    private static final String getSingularInputAssemblyText = """
getSingularInput:
\t
	push r2
	push r3
	push r4
	push r5
	push r6
	push r9
	push r10
	push r11
	push r12
	push r18
	push XL
	push XH
\t
	ldi XL, low(parameters)
	ldi XH, high(parameters)
	adiw X, 1
	ld r2, X+
	ld r3, X+ ; r2:r3 hold x calibration
	ld r4, X+
	ld r5, X+ ; r4:r5 hold y calibration
	ld r6, X
\t
	; Reading value of x axis
	ldi r16, (1<<REFS0)
	sts ADMUX, r16

	ldi r16, (1<<ADEN)|(1<<ADPS0)|(1<<ADPS1)|(1<<ADPS2)
	sts ADCSRA, r16

	lds r16,ADCSRA
	ori r16, (1<<ADSC)
	sts ADCSRA, r16
	readingDelayX:
		lds r16,ADCSRA
		sbrc r16, ADSC
		rjmp readingDelayX

	lds r10, ADCL
	lds r9, ADCH
	; r9:r10 now has the 10 bit result of the operation
\t
	nop
	nop
	nop
	nop ; delay for resetting time

	; reading yaxis
	ldi r16, (1<<REFS0)|1
	sts ADMUX, r16

	lds r16,ADCSRA
	ori r16, (1<<ADSC)
	sts ADCSRA, r16
	readingDelayY:
		lds r16,ADCSRA
		sbrc r16, ADSC
		rjmp readingDelayY

	lds r12, ADCL
	lds r11, ADCH
	; r11:r12 now has the 10 bit result of the operation

	; reading the select button
	sbic PIND, 0
	jmp AXIS_CONTROL_DETERMINER\t
	jmp SELECT_BUTTON_PRESS

	AXIS_CONTROL_DETERMINER:
		; Check for left axis
		add r3, r6
		clr r18
		adc r2, r18
		cp r10, r3
		cpc r9, r2
		brsh LEFT_AXIS_RETURN
	\t
		;RESET OF R2:R3
		sub r3, r6
		sbc r2, r18
	\t
		; Check for right axis
		sub r3, r6
		sbc r2, r18
		cp r10, r3
		cpc r9, r2
		brlo RIGHT_AXIS_RETURN

		; Check for up axis
		add r5, r6
		adc r4, r18
		cp r12, r5
		cpc r11, r4
		brsh UP_AXIS_RETURN

		;RESET OF R2:R3
		sub r5, r6
		sbc r4, r18

		; Check for down axis
		sub r5, r6
		sbc r4, r18
		cp r12, r5
		cpc r11, r4
		brlo DOWN_AXIS_RETURN
		rjmp NO_ACTION_RETURN


	SELECT_BUTTON_PRESS:
		ldi r16, 1
		rjmp RETURN_FUNC

	LEFT_AXIS_RETURN:
		ldi r16, 5
		rjmp RETURN_FUNC

	RIGHT_AXIS_RETURN:
		ldi r16, 3
		rjmp RETURN_FUNC

	UP_AXIS_RETURN:
		ldi r16, 2
		rjmp RETURN_FUNC

	DOWN_AXIS_RETURN:
		ldi r16, 4
		rjmp RETURN_FUNC

	NO_ACTION_RETURN:
		ldi r16, 0
		rjmp RETURN_FUNC

	RETURN_FUNC:
		pop XH
		pop XL
		pop r18
		pop r12
		pop r11
		pop r10
		pop r9
		pop r6
		pop r5
		pop r4
		pop r3
		pop r2
		ret
""";

    private static final String getRandomNumberAssemblyText = """
getRandomNumber:
    
    ; AVcc reference
    ; Left adjust result
    ; Select ADC4 (A4)
    ldi r16, (1<<REFS0)|(1<<ADLAR)|4
    sts ADMUX, r16

    ; Enable ADC
    ; Start conversion
    ; Prescaler = 128
    ldi r16, (1<<ADEN)|(1<<ADSC)|(1<<ADPS0)|(1<<ADPS1)|(1<<ADPS2)
    sts ADCSRA, r16

waitADC:
    lds r16, ADCSRA
    sbrc r16, ADSC
    rjmp waitADC

    ; Read result
    lds r16, ADCH

    ret
""";

    private static final String screenDataTransferAssemblyText= """
spi_init:
	ldi r17, (1<<SCK)|(1<<MOSI)|(1<<SS)|(1<<E_PIN)
	out DDRB, r17
	ldi r17, (1<<2)
	out DDRD, r17
	cbi PORTB, SS
	
	ldi r17, (1<<SPE)|(1<<MSTR)|(1<<SPR0)
	out SPCR, r17
	
	ldi r17, 0x00
	rcall spi_send
	ret

spi_send:
	out SPDR, r16
	rcall spi_wait
	
	
	sbi PORTB, SS
	nop
	nop
	cbi PORTB, SS

	nop
	nop
	nop

	sbi PORTB, E_PIN
	nop
	nop
	nop
	nop
	cbi PORTB, E_PIN

	rcall delay_40us
	ret

spi_wait:
	in r16, SPSR
	sbrs r16, SPIF
	rjmp spi_wait
	ret


; LCD TRANSMISSION SUBROUTINES
lcd_sendCommand:
	push r16
	
	andi r16, 0xF0
	cbr r16, (1 << RS_PIN)
	rcall spi_send
	
	pop r16

	swap r16
	andi r16, 0xF0
	cbr r16, (1 << RS_PIN)
	rcall spi_send
	
	rcall delay_40us
	ret


lcd_sendData:
	push r16
	
	andi r16, 0xF0
	sbr r16, (1 << RS_PIN)
	rcall spi_send
	
	pop r16

	swap r16
	andi r16, 0xF0
	sbr r16, (1 << RS_PIN)
	rcall spi_send
	
	rcall delay_40us
	ret

; SCREEN INITIALISATION SUBROUTINE
screenInitProcess:
	rcall delay_15ms
	rcall delay_15ms
	rcall delay_15ms
	push r23
	ldi r23, 3
	magicScreenWakeUpLoop:
		ldi r16, 0b00110000
		rcall spi_send
		rcall delay_5ms
		dec r23
		brne magicScreenWakeUpLoop
	
	pop r23
	; set to 4-bit mode (still in 8 bit interface)
	ldi r16, 0b00100000
	rcall lcd_sendCommand

	; Set Display lines and font
	ldi r16, 0b00101000
	rcall lcd_sendCommand

	;Display Off
	ldi r16, 0b00001000
	rcall lcd_sendCommand

	;Display Clear
	ldi r16, 0b00000001
	rcall lcd_sendCommand
	rcall delay_15ms

	;Entry mdoe Set
	ldi r16, 0b00000110
	rcall lcd_sendCommand
	
	;Display On
	ldi r16, 0b00001100
	rcall lcd_sendCommand

	ret
;  INITIALSATION FINISHED

initCustomCharacters:
	ldi ZL, low(AnimationFrames<<1)
	ldi ZH, high(AnimationFrames<<1)
	
	ldi r17, 64
	
	ldi r16, 0x40
	rcall lcd_sendCommand

	lpm r16, Z+
	loop1:
		lpm r16, Z+
		rcall lcd_sendData
		dec r17
		brne loop1
	ret


clearDisplay:
	;Display Clear
	ldi r16, 0b00000001
	rcall lcd_sendCommand

	rcall delay_15ms
	ret
""";

    private static final String gameDrawAssemblyText= """
            gameDraw:
            	;!!!!! IMPORTANT NOTE: R16 SHOULD HOLD THE PURE TRANSFER DATA WHEN IT IS TIME TO CALL LCD_SENDDATA COMMAND  !!!!!
            	push r16
            	push r17
            	push r18
            	push XL
            	push XH
            
            	// Set operating mode of screen to DDRAM DATA SAVE
            	ldi r16, 0x80
            	rcall lcd_sendCommand
            
            	// Set loop counter
            	ldi r17, 16
            
            	// set ram address to drawList
            	ldi XL, low(drawList)
            	ldi XH, high(drawList)
            
            	// set row Mode
            	ldi r18,0x01
            
            
            	; LOOPS
            	row1DrawLoop:
            		rcall delay_5ms
            		ld r16, X+
            		cpi r16, 0x00  ; Compare with 0x00 as 0x00 is designated empty character
            		breq emptySpace
            
            		sbrs r16, 7 ;  runs animationframe when bit 7 (msb) is set, which is signifying strings datatype
            		rjmp AnimationFramePrint
            		rjmp StringsPrintMain
            
            	row2DrawLoop:
            		rcall delay_5ms
            		ld r16, X+
            		cpi r16, 0x00  ; Compare with 0x00 as 0x00 is designated empty character
            		breq emptySpace
            
            		sbrs r16, 7 ;  runs animationframe when bit 7 (msb) is set, which is signifying strings datatype
            		rjmp AnimationFramePrint
            		rjmp StringsPrintMain
            
            	; CHARACTER PRINT
            	emptySpace:
            		ldi r16, 0x20 ;  code for empty in screen both a00 and a02 character maps
            		rcall lcd_sendData
            		rjmp loopEnd
            
            	AnimationFramePrint:
            		dec r16    ;  r16 is currently flash address, sub 1 and then div 8 to get CGRAM address of character
            		lsr r16
            		lsr r16
            		lsr r16
            		rcall lcd_sendData
            		rjmp loopEnd
            
            	StringsPrintMain:
            		push ZL
            		push ZH
            		push r20
            		push r21
            
            		mov ZL, r16\s
            		ldi ZH,0x00
            
            		adiw Z, 52
            
            
            		lsl ZL
            		rol ZH
            		\s
            		lpm r16, Z+  ; get string storage identifier byte
            
            		mov r20, r16
            		andi r20, 0b11100000 ; now r20 holds identifier bits of the string
            
            		lds r21, strMut
            		andi r21, 0b00011100
            		lsr r21
            		lsr r21
            
            		cp r20, r21
            		breq StringWithMutation
            
            		StringWithoutMutation:
            
            			mov r20, r16
            			andi r20, 0b00011111
            
            			inc r17
            			sbiw X, 1
            			loopStrDraw:
            				cpi r17, 0x01
            				breq adjustScreenRow
            				rjmp writeStr1
            				adjustScreenRow:
            					ldi r17, 16
            					ldi r18, 2
            					ldi r16, 0xC0
            					rcall lcd_sendCommand
            				writeStr1:
            					dec r17
            					adiw X, 1
            					lpm r16, Z+
            					rcall lcd_sendData
            					dec r20
            					brne loopStrDraw
            					pop r21
            					pop r20
            					pop ZH
            					pop ZL
            					rjmp loopEnd
            
            
            		StringWithMutation:
            			push YL
            			push YH
            			push r3
            			push r4
            			push r5
            
            			mov r20, r16
            			andi r20, 0b00011111
            
            			ldi YL, low(strMut)
            			ldi YH, high(strMut)
            
            			ld r21, Y+
            			andi r21, 0x03 ; get upper 2 bits of starting mutation index
            			lsl r21
            			lsl r21
            			lsl r21
            			mov r3, r21
            
            			ld r21, Y
            			andi r21, 0b11100000
            			lsr r21
            			lsr r21        ; get lower 3 bits of starting mutation index
            			lsr r21
            			lsr r21
            			lsr r21
            			add r3, r21
            
            			ld r21, Y
            			andi r21, 0b00011111   ; get length to of mutation
            			mov r4, r21
            
            			inc r17
            			sbiw X, 1
            			loopStrDrawMut:
            				cpi r17, 0x01
            				breq adjustScreenRow2
            				rjmp writeStr2
            				adjustScreenRow2:
            					ldi r17, 16
            					ldi r18, 2
            					ldi r16, 0xC0
            					rcall lcd_sendCommand
            
            				writeStr2:
            					dec r17
            					inc r5
            					adiw X, 1
            
            					cp r5, r3
            					breq readMut
            
            					readNormal:
            						lpm r16, Z+
            						rcall lcd_sendData
            						rjmp endLoopStrDrawMut
            
            					readMut:
            						ld r16, Y+
            						rcall lcd_sendData
            						dec r17
            						adiw X, 1
            						dec r4
            						brne readMut
            
            					endLoopStrDrawMut:
            						dec r20
            						brne loopStrDrawMut
            						pop r21
            						pop r20
            						pop ZH
            						pop ZL
            						pop r5
            						pop r4
            						pop r3
            						pop YH
            						pop YL
            						rjmp loopEnd
            
            	; END FUNCTIONS
            	loopEnd:
            		cpi r18, 0x02
            		brsh row2
            		rjmp row1
            
            		row1:
            			dec r17
            			brne row1DrawLoopJmp
            
            			ldi r17, 16
            
            			ldi r18,0x02      ; set row counter to 2, reset r17 to 16 and send command for setting pointer to 0xC0
            			ldi r16, 0xC0
            			rcall lcd_sendCommand
            
            		row2:
            			dec r17
            			brne row2DrawLoopJmp
            			rjmp funcFinish
            
            		row1DrawLoopJmp:
            			jmp row1DrawLoop
            
            		row2DrawLoopJmp:
            			jmp row2DrawLoop
            
            	funcFinish:
            		pop XH
            		pop XL
            		pop r18
            		pop r17
            		pop r16
            		rcall drawListZeroOut
            
            		ret
""";

    private static final String showFuncImageAssemblyText= """
            showFuncImage:  ;parameters are stored in r16,r17,r18, representing the AnimationFrame pointer, x loc, y loc respectively
                cpi r17, 16  ; checking if x loc is below 16
                brge showFuncXOverFlowError ;branching
                                       \s
                cpi r18, 2  ; checking if y loc is below 1
                brge showFuncYOverFlowError ;branching
            
                ; Below code use for locating the draw list location the pointer\s
                ; r16 should be stored in
                                   \s
                ldi r19, 16
            	mul r18, r19  ; y*16 + x
                add r0, r17
            
                ldi XL, low(drawList)
                ldi XH, high(drawList)
                                       \s
                add XL, r0
                adc XH, r1
            	clr r1
            
                ; Storing in correct location
                st X, r16
            
                ret
            
            showFuncXOverFlowError:
            	nop
            
            showFuncYOverFlowError:
            	nop
""";

    private static final String entryInitAssemblyText = """
entryInit:
    clr r0
	clr r1
	clr r2
	clr r3
	clr r4
	clr r5
	clr r6
	clr r7
	clr r8
	clr r9
	clr r10
	clr r11
	clr r12
	clr r13
	clr r14
	clr r15
	clr r16
	clr r17
	clr r18
	clr r19
	clr r20
	clr r21
	clr r22
	clr r23
	clr r24
	clr r25
	clr r26
	clr r27
	clr r28
	clr r29
	clr r30
	clr r31

	ldi r16, LOW(RAMEND)
    out SPL, r16
    ldi r16, HIGH(RAMEND)
    out SPH, r16
	
	cli
	rcall drawListZeroOut
	; screen init processes
	rcall spi_init
	rcall screenInitProcess
	rcall delay_15ms
	rcall delay_15ms
	rcall initCustomCharacters
	
	ldi XL, low(parameters)
	ldi XH, high(parameters)
	
	; game Draw flag init
	ldi r16, 0x00
	st X+, r16
	
	; joystick init
	ldi r16, high(511)
	st X+, r16
	ldi r16, low(511)
	st X+, r16
	ldi r16, high(511)
	st X+, r16
	ldi r16, low(511)
	st X+, r16
	ldi r16, 100
	st X, r16
	
	; clear numList
	rcall clearNumList
	sei
	
	call gameStart

	rjmp gameLoop
""";

    public static String getCommonResourcesAssemblyText() {
        return commonResourcesAssemblyText;
    }

    public static void setCommonResourcesAssemblyText(String data) {
        commonResourcesAssemblyText = data;
    }


    public static String getGetSingularInputAssemblyText() {
        return getSingularInputAssemblyText;
    }

    public static String getGetRandomNumberAssemblyText() {
        return getRandomNumberAssemblyText;
    }

    public static String getScreenDataTransferAssemblyText() {
        return screenDataTransferAssemblyText;
    }

    public static String getGameDrawAssemblyText() {
        return gameDrawAssemblyText;
    }

    public static String getShowFuncImageAssemblyText() {
        return showFuncImageAssemblyText;
    }

    public static String getEntryInitAssemblyText(){
        return entryInitAssemblyText;
    }

    public static String getSpecAssemblyText() {
        return specAssemblyText;
    }

}
