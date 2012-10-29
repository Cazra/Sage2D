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
		title = "Alpha Map test example"
		contents = new TransparencyPanel
		visible = true
	}
}


class TransparencyPanel extends GamePanel {
	background = new Color(0xffffff)
	preferredSize = new Dimension(640,480)
	
    changeLevel("main")
	
	def mainPaint(g : Graphics2D) : Unit = {
		// store affine transforms for later use
		val origTrans = g.getTransform
		
		curLevel.render(g)
        
		// restore the original transform
		g.setTransform(origTrans)
		
		// display HUD information
		g.setColor(new Color(0x000000))
		g.drawString("" + timer.fpsCounter, 10,32)
	}
	
	def loadingPaint(g : Graphics2D) : Unit = {
        // do nothing
	}
	
    
    // only makes ExampleLevel instances for "main"
    override def makeLevelInstance(levelName : String) : Level = {
        if(levelName == "main")
            new ExampleLevel(this)
        else 
            null
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
	
    def animate : Unit = {
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
     * In this example, the only image that is loaded into the cache is "ball",
     * in which all 0xff00ff colored pixels are completely transparent and an alpha map
     * is applied.
     */
    def loadImages(il : ImageLoader) : Unit = {
        // make 0xff00ff pixels completely transparent
        val transImg = ImageEffects.transparentColor(rawImage, new Color(0xff00ff))
        
        //get the rgb portion of the image
        val rgbImg = ImageLoader.getCroppedImage(transImg, 0,0,32,32)
        
        // get the alpha map for the image
        val alphaImg = ImageLoader.getCroppedImage(transImg, 32,0,32,32)
        
        // Apply the alpha map to our rgb image to produce our completed image!
        val finishedImg = ImageEffects.alphaMap(rgbImg, alphaImg, 32, 32)
        imgCache += ("ball" -> finishedImg)
        il.addImage(finishedImg)
        il.waitForAll
    }
    
    /** Empties the imgCache so that the cached images are no longer taking up memory. */
    def clean : Unit = {
        imgCache.clear
    }
}   


class ExampleLevel(game : GamePanel, parent : Level = null) extends Level(game, parent) {
    
    // create the balls and give them random sizes, velocities, and starting positions.
	val balls = new ListBuffer[BallSprite]
	for(i <- 1 to 100) {
		val x = Random.nextInt(640)
		val y = Random.nextInt(480)
		balls += new BallSprite(x, y, Random.nextDouble()*64 + 4)
	}
    
    
    def loadData : Unit = {
        // Preload the ball's image.
        BallSprite.loadImages(game.imgLoader)
    }
    
    def clean : Unit = {
        // do nothing.
    }
    
    def logic : Unit = {
        // move the balls
		for(ball <- balls) {
			ball.move(game)
		}
        
        // lastly, have all the balls call their animate method to determine the image they should render.
        // In the case of this example, they only show 1 image.
        for(ball <- balls) {
            ball.animate
        }
    }
    
    def render(g : Graphics2D) : Unit = {
        for(ball <- balls) {
			ball.render(g)
		}
    }
}

