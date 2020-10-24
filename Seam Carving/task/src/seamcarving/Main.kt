package seamcarving

import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities
import kotlin.math.pow
import kotlin.math.sqrt


val sc = Scanner(System.`in`)
var inputFile = ""
var fileName = ""
var enlist: MutableList<Double> = mutableListOf<Double>()
var enlistMin: MutableList<Double> = mutableListOf<Double>()
var enlistMinH: MutableList<Double> = mutableListOf<Double>()
var minPath: MutableList<Int> = mutableListOf<Int>()
var minPathH: MutableList<Int> = mutableListOf<Int>()
var horS: MutableList<Array<Int>> = mutableListOf<Array<Int>>()
var horSH: MutableList<Array<Int>> = mutableListOf<Array<Int>>()
var width = 100
var height = 50

fun main(args: Array<String>) {
    processArgs(args)
    var image = loadImage()
    /*if (image != null) {
        byteArrayToImage(negative(image))
    }*/
    //val me = maxEnergy(image!!)
    //calcMinEnergy(image!!)
    //println(enlistMin.size)
    //println(me)
    //val im = pixelEnergy(image!!, me)
    //val im = verticalSeam(image!!)
    //val im = horSeam(image!!)
    /*if (image != null) {
        seam(image)
    }*/
    //val im = vs(image!!)
    repeat(width) {
        enlist.clear()
        horS.clear()
        horSH.clear()
        image = resize(image!!)
    }
    repeat(height) {
        enlist.clear()
        horS.clear()
        horSH.clear()
        image = resizeH(image!!)
    }
    displayImage(image!!, "hello")
    if (image != null) {
        byteArrayToImage(image!!)
    }

}

fun loadImage(): BufferedImage? {
    try {
        val f = File(inputFile)
        return ImageIO.read(f)
    } catch (e: IOException) {
        println(e)
    }
    return null
}

fun isSeam(x: Int, y: Int): Boolean {
    for (coordin in horS) {
        if (coordin[0] == x && coordin[1] == y) return true
    }
    return false
}

fun isSeamH(x: Int, y: Int): Boolean {
    for (coordin in horSH) {
        if (coordin[0] == x && coordin[1] == y) return true
    }
    return false
}

fun resize(image: BufferedImage): BufferedImage {
    seam(image)
    val resizeImage = BufferedImage(image.getWidth() - 1, image.getHeight(), BufferedImage.TYPE_INT_RGB)
    for (y in 0 until image.getHeight()) {
        var xr = 0
        for (x in 0 until image.getWidth()) {
            if (isSeam(x,y)) continue
                var p: Int = image.getRGB(x, y)
                resizeImage.setRGB(xr, y, p)
                xr++
        }
    }
    return resizeImage
}

fun resizeH(image: BufferedImage): BufferedImage {
    seam(image)
    val resizeImage = BufferedImage(image.getWidth(), image.getHeight()-1, BufferedImage.TYPE_INT_RGB)
    for (x in 0 until image.getWidth()) {
        var yr = 0
        for (y in 0 until image.getHeight()) {
            if (isSeamH(x,y)) continue
            var p: Int = image.getRGB(x, y)
            resizeImage.setRGB(x, yr, p)
            yr++
        }
    }
    return resizeImage
}

fun processArgs(args: Array<String>) {
    if (args.size > 0) {
        for (i in 0 until args.size) {
            when (args[i]) {
                "-in" -> inputFile += args[i+1]
                "-out" -> fileName = args[i+1]
                "-width" -> width = args[i+1].toInt()
                "-height" -> height = args[i+1].toInt()
            }
        }
    }
}


fun byteArrayToImage(image: BufferedImage) {
    //val bImage = ImageIO.read(File("sample.jpg"))
    val bos = ByteArrayOutputStream()
    ImageIO.write(image, "png", bos)
    val data = bos.toByteArray()
    val bis = ByteArrayInputStream(data)
    val bImage2 = ImageIO.read(bis)
    ImageIO.write(bImage2, "png", File(fileName))
    //println("image created")
}

fun negative(img: BufferedImage): BufferedImage {
    for (y in 0 until img.getHeight()) {
        for (x in 0 until img.getWidth()) {
            var p: Int = img.getRGB(x, y)
            var a = p shr 24 and 0xff
            var r = p shr 16 and 0xff
            var g = p shr 8 and 0xff
            var b = p and 0xff
            r = 255 - r
            g = 255 - g
            b = 255 - b
            p = a shl 24 or (r shl 16) or (g shl 8) or b
            img.setRGB(x, y, p)
        }
    }
    return img
}

fun calculateEnergy(img: BufferedImage):Array<Array<Double>> {
    val energy = Array(img.width) { Array(img.height) { 0.0 } }
    var total = 0.0
    var e = 0.0
    for (y in 0 until img.height) {
        for (x in 0 until img.width) {
            when {
                (x == 0 && y == 0) -> {
                    e = calcEnergy(img.getRGB(0, 0), img.getRGB(2, 0),
                            img.getRGB(0, 0), img.getRGB(0, 2))
                }
                (x == 0 && y == img.getHeight() - 1) -> {
                    e = calcEnergy(img.getRGB(0, y), img.getRGB(2, y),
                            img.getRGB(x, img.getHeight() - 3), img.getRGB(x, img.getHeight() - 1))
                }
                (x == img.getWidth() - 1 && y == 0) -> {
                    e = calcEnergy(img.getRGB(img.getWidth() - 3, y), img.getRGB(img.getWidth() - 1, y),
                            img.getRGB(x, 0), img.getRGB(x, 2))
                }
                (x == img.getWidth() - 1 && y == img.getHeight() - 1) -> {
                    e = calcEnergy(img.getRGB(img.getWidth() - 3, y), img.getRGB(img.getWidth() - 1, y),
                            img.getRGB(x, img.getHeight() - 3), img.getRGB(x, img.getHeight() - 1))
                }
                (x == 0 && y in 1..img.getHeight() - 2) -> {
                    e = calcEnergy(img.getRGB(0, y), img.getRGB(2, y),
                            img.getRGB(x, y - 1), img.getRGB(x, y + 1))
                }
                (x in 1..img.getWidth() - 2 && y == 0) -> {
                    e = calcEnergy(img.getRGB(x - 1, y), img.getRGB(x + 1, y),
                            img.getRGB(x, 0), img.getRGB(x, 2))
                }
                (x == img.getWidth() - 1 && y in 1..img.getHeight() - 2) -> {
                    e = calcEnergy(img.getRGB(img.getWidth() - 3, y), img.getRGB(img.getWidth() - 1, y),
                            img.getRGB(x, y - 1), img.getRGB(x, y + 1))
                }
                (y == img.getHeight() - 1 && x in 1..img.getWidth() - 2) -> {
                    e = calcEnergy(img.getRGB(x - 1, y), img.getRGB(x + 1, y),
                            img.getRGB(x, img.getHeight() - 3), img.getRGB(x, img.getHeight() - 1))
                }
                (x in 1..img.getWidth() - 2 && y in 1..img.getHeight() - 2) -> {
                    e = calcEnergy(img.getRGB(x - 1, y), img.getRGB(x + 1, y),
                            img.getRGB(x, y - 1), img.getRGB(x, y + 1))
                }
            }
            energy[x][y] =e
            if (energy[x][y] > total) total = energy[x][y]

        }

    }
    //println ("total is $total")
    return energy
}

fun seam(img: BufferedImage) {
    val en = calculateEnergy(img)
    val enMin = Array(img.width) { Array(img.height) { 0.0 } }
    for (y in 0 until img.getHeight()) {
        for (x in 0 until img.getWidth()) {
            if (y == 0) {
                enMin[x][y] = en[x][y]
            } else {
                when (x) {
                    0 -> enMin[x][y] = kotlin.math.min(en[x][y] + enMin[x][y - 1],
                            en[x][y] + enMin[x + 1][y - 1])
                    (img.getWidth() - 1) -> enMin[x][y] = kotlin.math.min(en[x][y] + enMin[x][y - 1],
                            en[x][y] + enMin[x - 1][y - 1])
                    else -> {
                        enMin[x][y] = kotlin.math.min(en[x][y] + enMin[x][y - 1], kotlin.math.min(en[x][y] + enMin[x - 1][y - 1],
                                en[x][y] + enMin[x + 1][y - 1]))
                    }
                }
            }
        }
    }
    var maxVal = Double.MAX_VALUE
    var endNode = 0

    for (x in 0 until img.getWidth()) {
        if (enMin[x][img.getHeight() - 1] < maxVal) {
            maxVal = enMin[x][img.getHeight() - 1]
            endNode = x
        }
    }
    //println("x is $endNode")
    horS.add(arrayOf(endNode, img.getHeight() - 1))

    for (y in img.getHeight() - 1 downTo 1) {
        when {
            endNode  == 0 -> {
                if (en[endNode][y - 1] < en[endNode + 1][y - 1]) horS.add(arrayOf(endNode, y - 1))
                endNode = endNode + 1
            }
            endNode == img.getWidth() - 1 -> {
                if (en[endNode][y - 1] < en[endNode - 1][y - 1]) horS.add(arrayOf(endNode, y - 1))
                endNode = endNode - 1
            }
            else -> {
                when {
                    (enMin[endNode][y - 1] < enMin[endNode - 1][y - 1]) && (enMin[endNode][y - 1] < enMin[endNode + 1][y - 1]) ->
                        endNode = endNode

                    (enMin[endNode - 1][y - 1] < enMin[endNode][y - 1]) && (enMin[endNode - 1][y - 1] < enMin[endNode + 1][y - 1]) ->
                        endNode = endNode - 1

                    (enMin[endNode + 1][y - 1] < enMin[endNode - 1][y - 1]) && (enMin[endNode + 1][y - 1] < enMin[endNode][y - 1]) ->
                        endNode = endNode + 1

                    (enMin[endNode][y - 1] == enMin[endNode - 1][y - 1]) && (enMin[endNode + 1][y - 1] < enMin[endNode][y - 1]) ->
                        endNode = endNode + 1

                    (enMin[endNode][y - 1] == enMin[endNode + 1][y - 1]) && (enMin[endNode][y - 1] < enMin[endNode][y - 1]) ->
                        endNode = endNode

                    (enMin[endNode - 1][y - 1] == enMin[endNode][y - 1]) && (enMin[endNode + 1][y - 1] < enMin[endNode][y - 1]) ->
                        endNode = endNode + 1

                    (enMin[endNode - 1][y - 1] == enMin[endNode + 1][y - 1]) && (enMin[endNode][y - 1] < enMin[endNode - 1][y - 1]) ->
                        endNode = endNode

                    (enMin[endNode][y - 1] == enMin[endNode - 1][y - 1]) && (enMin[endNode][y - 1] == enMin[endNode + 1][y - 1]) ->
                        endNode = endNode

                    (enMin[endNode][y - 1] == enMin[endNode + 1][y - 1]) && (enMin[endNode - 1][y - 1] > enMin[endNode][y - 1]) ->
                        endNode = endNode + 1

                    (enMin[endNode][y - 1] == enMin[endNode - 1][y - 1]) && (enMin[endNode + 1][y - 1] > enMin[endNode][y - 1]) ->
                        endNode = endNode - 1

                    (enMin[endNode - 1][y - 1] == enMin[endNode + 1][y - 1]) && (enMin[endNode][y - 1] > enMin[endNode - 1][y - 1]) ->
                        endNode = endNode + 1

                    //else -> println("${en[endNode][y - 1]} -- ${en[endNode - 1][y - 1]} -- ${en[endNode + 1][y - 1]}")
                }
            }
        }
        horS.add(arrayOf(endNode, y - 1))
    }

    //horizont
    val enMinH = Array(img.width) { Array(img.height) { 0.0 } }
    for (x in 0 until img.getWidth()) {
        for (y in 0 until img.getHeight()) {
            if (x == 0) {
                enMinH[x][y] = en[x][y]
            } else {
                when (y) {
                    0 -> enMinH[x][y] = kotlin.math.min(en[x][y] + enMinH[x - 1][y],
                            en[x][y] + enMinH[x - 1][y + 1])
                    (img.getHeight() - 1) -> enMinH[x][y] = kotlin.math.min(en[x][y] + enMinH[x - 1][y - 1],
                            en[x][y] + enMinH[x - 1][y])
                    else -> {
                        enMinH[x][y] = kotlin.math.min(en[x][y] + enMinH[x - 1][y], kotlin.math.min(en[x][y] + enMinH[x - 1][y - 1],
                                en[x][y] + enMinH[x - 1][y + 1]))
                    }
                }
            }
        }
    }

    maxVal = Double.MAX_VALUE
    endNode = 0

    for (y in 0 until img.getHeight()) {
        if (enMinH[img.getWidth() - 1][y] < maxVal) {
            maxVal = enMinH[img.getWidth() - 1][y]
            endNode = y
        }
    }

    //println("End Node is -- $endNode")
    horSH.add(arrayOf(img.getWidth() - 1, endNode))

    var se = 0.0
    se += en[img.getWidth()-1][endNode]



    for (x in img.getWidth() - 1 downTo 1) {
        when {
            endNode  == 0 -> {
                //println("endNode is 0")
                when {
                    (enMinH[x - 1][endNode] < enMinH[x - 1][endNode + 1]) -> endNode = endNode
                    (enMinH[x - 1][endNode] > enMinH[x - 1][endNode + 1]) -> endNode = endNode + 1
                    (enMinH[x - 1][endNode] == enMinH[x - 1][endNode + 1]) -> endNode = endNode
                }
            }
            endNode == img.getHeight() - 1 -> {
                //println("endNode is ${img.getHeight() - 1}")
                when {
                    (enMinH[x - 1][endNode] < enMinH[x - 1][endNode - 1]) -> endNode = endNode
                    (enMinH[x - 1][endNode] > enMinH[x - 1][endNode - 1]) -> endNode = endNode - 1
                    (enMinH[x - 1][endNode] == enMinH[x - 1][endNode - 1]) -> endNode = endNode
                }

            }
            else -> {
                when {
                    /*(enMinH[x-1][endNode] < enMinH[x-1][endNode - 1]) && (enMinH[x-1][endNode] < enMin[x-1][endNode + 1]) ->
                        endNode = endNode*/

                    (enMinH[x-1][endNode - 1] < enMinH[x-1][endNode]) && (enMinH[x-1][endNode - 1] < enMinH[x-1][endNode + 1]) ->
                        endNode = endNode - 1

                    (enMinH[x-1][endNode + 1] < enMinH[x-1][endNode - 1]) && (enMinH[x-1][endNode + 1] < enMinH[x-1][endNode]) ->
                        endNode = endNode + 1

                    (enMinH[x-1][endNode] == enMinH[x-1][endNode - 1]) && (enMinH[x-1][endNode + 1] < enMinH[x-1][endNode]) ->
                        endNode = endNode + 1

                    (enMinH[x-1][endNode] == enMinH[x-1][endNode + 1]) && (enMinH[x-1][endNode] < enMinH[x-1][endNode - 1]) ->
                        endNode = endNode + 1//

                    (enMinH[x-1][endNode - 1] == enMinH[x-1][endNode]) && (enMinH[x-1][endNode + 1] < enMinH[x-1][endNode]) ->
                        endNode = endNode + 1

                    (enMinH[x-1][endNode - 1] == enMinH[x-1][endNode + 1]) && (enMinH[x-1][endNode] < enMinH[x-1][endNode - 1]) ->
                        endNode = endNode

                    (enMinH[x-1][endNode] == enMinH[x-1][endNode - 1]) && (enMinH[x-1][endNode] == enMinH[x-1][endNode + 1]) ->
                        endNode = endNode ////

                    (enMinH[x-1][endNode] == enMinH[x-1][endNode + 1]) && (enMinH[x-1][endNode - 1] > enMinH[x-1][endNode]) ->
                        endNode = endNode + 1//

                    (enMinH[x-1][endNode] == enMinH[x-1][endNode - 1]) && (enMinH[x-1][endNode + 1] > enMinH[x-1][endNode]) ->
                        endNode = endNode - 1 //

                    (enMinH[x-1][endNode - 1] == enMinH[x-1][endNode + 1]) && (enMinH[x-1][endNode] > enMinH[x-1][endNode - 1]) ->
                        endNode = endNode - 1 //
                    //else -> println("Unknown ${enMinH[x-1][endNode - 1]}  ${enMinH[x-1][endNode]}  ${enMinH[x-1][endNode + 1]}")
                }
            }
        }
        se += en[x-1][endNode]
        horSH.add(arrayOf(x-1, endNode))
        //println("SE -- $se -- Endnode is $endNode")
    }
    //println("Start Node is -- $endNode")

}

fun vs(img: BufferedImage): BufferedImage{

   /* for (coord in horS) {
        var p = 255 shl 24 or (255 shl 16) or (0 shl 8) or 0
        img.setRGB(coord[0], coord[1], p)
    }*/
    for (coordin in horSH) {
        var p = 255 shl 24 or (255 shl 16) or (0 shl 8) or 0
        img.setRGB(coordin[0], coordin[1], p)
    }
    return img
}

fun pixelEnergy(img: BufferedImage, mE: Double): BufferedImage{
    var count = 0
    for (y in 0 until img.getHeight()) {
        for (x in 0 until img.getWidth()) {

            var energy = enlist[count]
            count++
            var intense = (255.0 * energy / mE).toInt()
            var p = intense shl 24 or (intense shl 16) or (intense shl 8) or intense
            img.setRGB(x, y, p)
        }
    }
    return img
}

fun calcEnergy(pl: Int, pr: Int, pt: Int, pd: Int): Double {
    var al = pl shr 24 and 0xff
    var rl = pl shr 16 and 0xff
    var gl = pl shr 8 and 0xff
    var bl = pl and 0xff
    var ar = pr shr 24 and 0xff
    var rr = pr shr 16 and 0xff
    var gr = pr shr 8 and 0xff
    var br = pr and 0xff
    var at = pt shr 24 and 0xff
    var rt = pt shr 16 and 0xff
    var gt = pt shr 8 and 0xff
    var bt = pt and 0xff
    var ad = pd shr 24 and 0xff
    var rd = pd shr 16 and 0xff
    var gd = pd shr 8 and 0xff
    var bd = pd and 0xff
    var deltaH = Math.pow((rl - rr).toDouble(), 2.0) +
            Math.pow((gl - gr).toDouble(), 2.0) +
            Math.pow((bl - br).toDouble(), 2.0)
    var deltaV = (rt - rd).toDouble().pow(2.0) +
            (gt - gd).toDouble().pow(2.0) +
            (bt - bd).toDouble().pow(2.0)
    return sqrt(deltaH + deltaV)
}


class ImagePanel(val img: BufferedImage): JPanel() {

    override fun paint(g: Graphics?) {
        g!!.drawImage(img, 0, 0, this)
    }
}

private fun displayImage(img: BufferedImage, title: String) {
    SwingUtilities.invokeLater {
        val frame = JFrame(title)
        frame.setSize(img.width, img.height)
        frame.contentPane = ImagePanel(img)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.isVisible = true
    }
}