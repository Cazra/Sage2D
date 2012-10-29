/*======================================================================
 * 
 * SAGE 2D : Scala-Accelerated 2D Game Engine
 * 
 * Copyright (c) 2012 by Stephen Lindberg (sllindberg21@students.tntech.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
======================================================================*/

package sage2D

import swing.Panel
import java.awt.Graphics2D
import sage2D.input._
import sage2D.images.ImageLoader

/** 
 * A swing panel object for containing your application and rendering it. 
 * It also provides several helpful methods for managing logic flow, the timer, and painting.
 */

abstract class GamePanel extends Panel with java.awt.event.ActionListener {
	
	/** The panel's timer object for timing the execution of logic iterations and repaint calls */
	val timer : GameTimer = new GameTimer(0,this)
	
    /** Used for polling keyboard input */
    var keyboardIn = new KeyboardInput(this)
    
    /** Used for polling mouse input */
    var mouseIn = new MouseInput(this)
    
    /** The game's ImageLoader, used primarily to wait on filtered images to finish processing. */
    var imgLoader = new ImageLoader(this.peer)
    
	/** The timer event handler will execute this many logic iterations each time it gets a timer event. This can be increased to produce fast-forward type effects. */
    var stepsPerFrame = 1
	
	/** A flag for telling the panel whether it needs to wait to load something and display the loading animation instead of its normal execution */
	var isLoading : Boolean = false
	
    /** Flag for whether the game is currently running. If this is false, then all timer events will be skipped by the timer event handler. */
    var isRunning = false
    
    /** Our game/application's current level */
    var curLevel : Level = null
    
    /** Flag to let our game know that we need to change levels at the before performing any logic or rendering on an iteration. */
    var changingLevel = false
   
    /** The name of the level we are switching to if changingLevel is true. */
    var changeToLevelName = ""
    
	/** 
	 * A flag for telling components outside the panel that it is currently busy handling a timerLoop iteration or a repaint. 
	 * This is meant to encourage programming with concurrency in mind, but it may be replaced with some other concurrency mechanism
	 * in a later SAGE 2D build.
	 */
	var timerLock : Boolean = false
	
	/** A flag to tell paint whether or not to call FlowPanel's paint to clear the panel's canvas before each frame.*/
	var repaintBackground = true
	
	/** 
	 * Event handler for the timer object. 
	 * It runs an iteration through the timerLoop method if it isn't currently loading something, and it tells the panel to repaint.
	 */
	
	def actionPerformed(event : java.awt.event.ActionEvent) : Unit = event.getSource() match {
		case gt : GameTimer => {
			timerLock = true
			
            if(!this.isRunning) {
                timerLock = false
                return
            }
            
            // poll user input for this frame
            keyboardIn.poll
            mouseIn.poll
            
            // Run n iterations through our game's logic (most of the time, this will be 1.)
            // Then perform 1 rendering iteration.
            for(i <- 0 until stepsPerFrame) {
                logic
            }
			repaint
			gt.updateFrameRateCounter
			
			timerLock = false
		}
		case _ =>
	}
	
	
	
	/** 
	 * Checks if we need to change levels, changes levels if it needs to, and then performs one iteration through the application's logic. 
	 * The user is expected to implement their own logic for their application's needs, typically by
     * delegating to the logic code of their game's current level.
	 */
	def logic : Unit = {
        if(changingLevel)
            doChangeLevel
        
        // do some stuff
        // ...
        
        if(curLevel != null)
            curLevel.logic
    }
	
	
	/**
	 * Gets called whenever the application needs to repaint. 
	 * If isLoading is true, it calls the loadingPaint method.
	 * Otherwise, it calls the mainPaint method.
	 * @param g		This panel's graphics context passed down by repaint.
	 */
	 
	override def paint(g : Graphics2D) : Unit = {
		if(repaintBackground)	super.paint(g)
		
		if(!isLoading) 	mainPaint(g)
		else 			loadingPaint(g)
	}
	
	
	/**
	 * The top-level rendering method for the application's normal (nonloading) execution.
	 * The user is expected to implement their own mainPaint method for their application's needs.
	 * @param g		This panel's graphics context passed down by repaint.
	 */
	
	def mainPaint(g : Graphics2D) : Unit
	
	
	/**
	 * The top-level rendering method for displaying some sort of loading animation while the 
	 * application is busy loading or waiting on something.
	 * The user is expected to implement their own loadingPaint method for their application's needs.
	 * @param g		This panel's graphics context passed down by repaint.
	 */
	def loadingPaint(g : Graphics2D) : Unit
	
	
	/** 
	 * Begins the application and starts the timer 
	 * @param fps	The desired frame rate for the application's timer. 60 by default.
	 */
	def start(fps : Int = 60) : Unit = {
		timer.setFPS(fps)
		timer.start
        this.isRunning = true
	}
    
    
    /** 
     * Call this to perform any cleanup duties when the user is done with this GamePanel.
     */
	def clean : Unit = {
        // This does nothing by default. Override it to fit your needs.
    }
    
    /** Schedules the game to change to a different level on the next timer event. */
    def changeLevel(name : String) : Unit = {
        changingLevel = true
        changeToLevelName = name
    }
    
    /** 
     * Cleans up after the current level and then switches to a new level matching changeToLevelName before performing any logic or rendering on a game iteration.
     */
    def doChangeLevel : Unit = {
        isLoading = true
        changingLevel = false
        
        // TODO: create a thread that performs the level's resource loading for us so 
        // that we can display a loading screen instead of just freezing while it loads.
        
        if(curLevel != null)
            curLevel.clean
            
        var newLevel = makeLevelInstance(changeToLevelName)
        if(newLevel != null) {
            curLevel = newLevel
        }
        else {
            System.err.println("GamePanel change level error : " + changeToLevelName + " is not a valid level in this game.")
        }
        
        this.isLoading = false
    }
    
    /** 
     * Returns an instance of a level associated with the given level name. 
     * The user is expected to implement their own code for this method since 
     * no games are likely to have the same mapping of names to Level subtype instances.
     */
    def makeLevelInstance(levelName : String) : Level = {
        null
    }
}




