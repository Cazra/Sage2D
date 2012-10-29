package transparency

import swing.SimpleSwingApplication
import swing.MainFrame
import java.awt._
import java.awt.geom.Rectangle2D
import sage2D._
import sage2D.sprites.Sprite
import sage2D.images._

import util.Random
import collection.mutable.ListBuffer
import collection.mutable.HashMap


object TransparencyMain extends SimpleSwingApplication {
	
	/** The universal background color for the GUI's panels */
	val bgColor = new Color(0x000000)
	
	/** The window's Frame object */
	
	def top = new MainFrame {
		title = "Transparency test example"
		contents = new TransparencyPanel
		visible = true
	}
}


class TransparencyPanel extends GamePanel {
	background = new Color(0xffffff)
	preferredSize = new Dimension(640,480)
	
    // Preload the ball's image.
    BallSprite.loadImages(imgLoader)
    
    // create the balls and give them random sizes, velocities, and starting positions.
	val balls = new ListBuffer[BallSprite]
	for(i <- 1 to 2500) {
		val x = Random.nextInt(640)
		val y = Random.nextInt(480)
        val ball = new BallSprite(x, y, Random.nextDouble()*64 + 4)
        ball.setAlpha(Random.nextDouble)
		balls += ball
	}
    
	override def logic : Unit = {
		
        // move the balls
		for(ball <- balls) {
			ball.move(this)
		}
        
        // lastly, have all the balls call their animate method to determine the image they should render.
        // In the case of this example, they only show 1 image.
        for(ball <- balls) {
            ball.animate(imgLoader)
        }
        
        imgLoader.waitForAll
	}
	
	def mainPaint(g : Graphics2D) : Unit = {
		// store affine transforms for later use
		val origTrans = g.getTransform
		
		for(ball <- balls) {
			ball.render(g)
		}
		// restore the original transform
		g.setTransform(origTrans)
		
		// display HUD information
		g.setColor(new Color(0x000000))
		g.drawString("" + timer.fpsCounter, 10,32)
	}
	
	def loadingPaint(g : Graphics2D) : Unit = {
	
	}
	
	start()
}

class BallSprite(val _x : Double, val _y : Double, val radius : Double) extends Sprite(_x, _y) {
	var dia = 2 * radius
	var dx = Random.nextDouble()*6 - 3
	var dy = Random.nextDouble()*6 - 3
    scale(dia/32.0)
    focalX = 16.0
    focalY = 16.0
    
    var curImage : Image = null
	
    def animate(il : ImageLoader) : Unit = {
    /*    val semiTrans = (255*alphaMaster).toInt
        val imgKey = "ball" + semiTrans
        
        // try to load the semitransparent image from the cache. If it's not in the cache,
        // create the semitransparent image and put it in the cache.
        if(BallSprite.imgCache.contains(imgKey))
            curImage = BallSprite.imgCache("ball" + semiTrans)
        else {
            val semiTransImg = ImageEffects.setOpacity(BallSprite.imgCache("ball"), semiTrans)
            il.addImage(semiTransImg)
            curImage = semiTransImg
        }
        */
        
        curImage = BallSprite.imgCache("ball")
    }
    
	override def draw(g : Graphics2D) : Unit = {
        g.drawImage(curImage, null, null)
	}

	def move(panel : GamePanel) : Unit = {
		x += dx
		y += dy
		
		if(x < 0 && dx < 0) dx *= -1
		if(y < 0 && dy < 0) dy *= -1
		if(x > panel.size.width && dx > 0) dx *= -1
		if(y > panel.size.height && dy > 0) dy *= -1
        
        if(x > panel.size.width) x = panel.size.width
        if(y > panel.size.height) y = panel.size.height
	}

}

object BallSprite {
    /** the raw, unfiltered image for the balls. */
    val rawImage : Image = ResourceLoader.loadImage("ball.png")
    
    /** caches filtered images for reuse. */
    val imgCache = new HashMap[String, Image]
    
    /** 
     * Creates the filtered images and waits for them to finish loading. 
     */
    def loadImages(il : ImageLoader) : Unit = {
        val transImg = ImageEffects.transparentColor(rawImage, new Color(0xff00ff))
        imgCache += ("ball" -> transImg)
        il.addImage(transImg)
        il.waitForAll
    }
    
    /** Empties the imgCache so that the cached images are no longer taking up memory. */
    def clean : Unit = {
        imgCache.clear
    }
}   


