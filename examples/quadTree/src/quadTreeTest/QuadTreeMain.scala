package quadTreeTest

import swing.SimpleSwingApplication
import swing.MainFrame
import java.awt._
import java.awt.geom.Rectangle2D
import sage2D._
import sage2D.sprites._
import sage2D.images._
import sage2D.input._

import util.Random
import collection.mutable.ListBuffer


object QuadTreeMain extends SimpleSwingApplication {
	
	/** The universal background color for the GUI's panels */
	val bgColor = new Color(0x000000)
	
	/** The window's Frame object */
	
	def top = new MainFrame {
		title = "QuadTree test example"
		contents = new QuadTreeTestPanel
		visible = true
	}
}


class QuadTreeTestPanel extends GamePanel {
	background = new Color(0xffffff)
	preferredSize = new Dimension(640,480)
    
    /** The keyboard input interface for the panel */
	focusable = true
	
	val balls = new ListBuffer[BallSprite]
	for(i <- 1 to 500) {
		val x = Random.nextInt(640)
		val y = Random.nextInt(480)
		balls += new BallSprite(x, y, Random.nextDouble()*64 + 4)
	}
	val quadSprite = new QuadTreeSprite(null)
	
	override def logic : Unit = {
		// create a new quadtree
		val quadTree = new QuadTree(0,0,this.size.width,this.size.height)
		quadSprite.quadTree = quadTree
		
		// populate the quadtree
		for(ball <- balls) quadTree.insert(ball)
		
		for(ball <- balls) ball.collisionList = quadTree.query(ball)
		
		for(ball <- balls) {
		//	println("\n" + ball.collisionList + "\n")
			ball.isColliding = false
			for(sprite <- ball.collisionList) sprite match {
				case otherBall : BallSprite => 
					if(ball.collision(otherBall)) ball.isColliding = true
				case _ =>
			}
			ball.move(this)
            
            // keyboard test
            if(keyboardIn.isAnyPressed) { 
                    ball.x += 1 
                    ball.y += 1 
                }
		}
        
        
	}
	
	def mainPaint(g : Graphics2D) : Unit = {
		// store affine transforms for later use
		val origTrans = g.getTransform
		
		for(ball <- balls) {
			ball.render(g)
		}
		
		quadSprite.render(g)
		
		// restore the original transform
		g.setTransform(origTrans)
		
		// display HUD information
		g.setColor(new Color(0x000000))
		g.drawString("" + timer.fpsCounter, 10,32)
	}
	
	def loadingPaint(g : Graphics2D) : Unit = {
	
	}
	
	start()
   // timer.setDelay(6)
    println("timer delay: " + timer.getDelay)
}

class BallSprite(val _x : Double, val _y : Double, val radius : Double) extends Sprite(_x, _y) {
	var dia = 2 * radius
	var dx = Random.nextDouble()*6 - 3
	var dy = Random.nextDouble()*6 - 3
	
	var isColliding = false
	
	override def draw(g : Graphics2D) : Unit = {
		g.translate(0-radius,0-radius)
		g.setColor( if(isColliding) new Color(0xaaffaa) else new Color(0xffaaaa))
		g.fillOval(0, 0, dia.toInt, dia.toInt)
	}
	
	override def getCollisionBox : Rectangle2D = {
		new Rectangle2D.Double(x-radius, y-radius, dia, dia)
	}
	
	def move(panel : GamePanel) : Unit = {
		x += dx
		y += dy
		
		if(x < 0 && dx < 0) dx *= -1
		if(y < 0 && dy < 0) dy *= -1
		if(x > panel.size.width && dx > 0) dx *= -1
		if(y > panel.size.height && dy > 0) dy *= -1
	}
	
	def collision(other : BallSprite) : Boolean = {
		if(this == other) 
			false
		else
			(GameMath.dist(this.x, this.y, other.x, other.y) < this.radius + other.radius)
	}
}



class QuadTreeSprite(var quadTree : QuadTree) extends Sprite(0,0) {
	override def draw(g: Graphics2D) : Unit = {
		g.setColor(new Color(0xaa0000))
		
		recDraw(g,quadTree)
	}
	
	def recDraw(g: Graphics2D, quad : QuadTree) : Unit = {
		g.drawRect(quad.minX.toInt, quad.minY.toInt, (quad.maxX-quad.minX).toInt, (quad.maxY-quad.minY).toInt)
		for(subQuad <- quad.quads) {
			if(subQuad != null) recDraw(g,subQuad)
		}
	}
}


