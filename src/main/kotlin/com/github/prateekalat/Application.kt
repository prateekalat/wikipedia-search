package com.github.prateekalat

import com.github.prateekalat.index.IndexGenerator
import com.github.prateekalat.parse.PageParser
import com.github.prateekalat.tokenize.Cleaner
import com.github.prateekalat.tokenize.Stemmer
import com.github.prateekalat.tokenize.Tokenizer
import com.google.code.externalsorting.ExternalSort.mergeSortedFiles
import com.google.code.externalsorting.ExternalSort.sortInBatch
import io.reactivex.schedulers.Schedulers
import java.io.File
import javax.xml.parsers.SAXParserFactory

fun main(args: Array<String>) {

    if (args.size < 2) {
        System.err.println("Too few arguments. First argument is input file. Second is output file.")
        System.exit(1)
    }

    val inputFilePath = args[0]
    val outputFilePath = args[1]

    val inputFile = File(inputFilePath)
    val outputFile = File(outputFilePath)
    val outputDirectory = outputFile.parentFile

    val unsortedIndexFile = File("$outputDirectory/unsorted_index.txt").apply { deleteOnExit() }
    val writer = unsortedIndexFile.bufferedWriter()

    val parserFactory = SAXParserFactory.newInstance()
    val saxParser = parserFactory.newSAXParser()
    val pageParser = PageParser(saxParser)
    val cleaner = Cleaner(pageParser.getPages(inputFile))
    val tokenizer = Tokenizer(cleaner)
    val stemmer = Stemmer(tokenizer)
    val indexGenerator = IndexGenerator(stemmer)

    indexGenerator.generateIndex()
            .observeOn(Schedulers.io())
            .map {
//                System.out.println("I/O:  ${Thread.currentThread().id}")
                writer.write(it)
            }
            .blockingSubscribe(
                    {
//                        System.out.println("Blocking: ${Thread.currentThread().id}")
                    },
                    {
                        System.err.println(it)
                        writer.close()
                    },
                    {

                        val comparator: Comparator<String> = Comparator { o1, o2 ->
                            o1.split(":")[0].compareTo(o2.split(":")[0])
                        }

                        val listOfFiles = sortInBatch(unsortedIndexFile, comparator, outputDirectory)

                        mergeSortedFiles(listOfFiles, outputFile, comparator)

                        writer.close()
                    }
            )
}
