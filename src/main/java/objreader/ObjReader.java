package objreader;

import math.Vector2f;
import math.Vector3f;
import model.Model;
import model.Polygon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ObjReader {
    private static final String OBJ_VERTEX_TOKEN = "v";
    private static final String OBJ_TEXTURE_TOKEN = "vt";
    private static final String OBJ_NORMAL_TOKEN = "vn";
    private static final String OBJ_FACE_TOKEN = "f";

    public static Model read(String fileContent) {
        Model result = new Model();

        int lineInd = 0;
        Scanner scanner = new Scanner(fileContent);
        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine();
            List<String> wordsInLine = new ArrayList<>(Arrays.asList(line.split("\\s+")));
            if (wordsInLine.isEmpty()) {
                continue;
            }

            final String token = wordsInLine.get(0);
            wordsInLine.remove(0);

            ++lineInd;
            switch (token) {
                case OBJ_VERTEX_TOKEN -> result.vertices.add(parseVertex(wordsInLine, lineInd));
                case OBJ_TEXTURE_TOKEN -> result.textureVertices.add(parseTextureVertex(wordsInLine, lineInd));
                case OBJ_NORMAL_TOKEN -> result.normals.add(parseNormal(wordsInLine, lineInd));
                case OBJ_FACE_TOKEN -> result.polygons.add(parseFace(wordsInLine, lineInd));
                default -> {}
            }
        }
        result.checkCorrectOfData();
        return result;
    }

    protected static Vector3f parseVertex(final List<String> wordsInLineWithoutToken, int lineInd) {
        if (wordsInLineWithoutToken.size() != 3) {
            throw new ObjReaderException.ObjFormatException("The number of vertex coordinates is incorrect", lineInd);
        }

        try {
            return new Vector3f(
                    Float.parseFloat(wordsInLineWithoutToken.get(0)),
                    Float.parseFloat(wordsInLineWithoutToken.get(1)),
                    Float.parseFloat(wordsInLineWithoutToken.get(2)));

        } catch(NumberFormatException e) {
            throw new ObjReaderException.ObjFormatException("Failed to parse float value.", lineInd);

        } catch(IndexOutOfBoundsException e) {
            throw new ObjReaderException.ObjFormatException("Too few vertex arguments.", lineInd);
        }
    }

    protected static Vector2f parseTextureVertex(final List<String> wordsInLineWithoutToken, int lineInd) {
        if (wordsInLineWithoutToken.size() != 2 && wordsInLineWithoutToken.size() != 3) {
            throw new ObjReaderException.ObjFormatException("The number of texture vertex coordinates is incorrect", lineInd);
        }

        try {
            return new Vector2f(
                    Float.parseFloat(wordsInLineWithoutToken.get(0)),
                    Float.parseFloat(wordsInLineWithoutToken.get(1)));

        } catch(NumberFormatException e) {
            throw new ObjReaderException.ObjFormatException("Failed to parse float value.", lineInd);

        } catch(IndexOutOfBoundsException e) {
            throw new ObjReaderException.ObjFormatException("Too few texture vertex arguments.", lineInd);
        }
    }

    protected static Vector3f parseNormal(final List<String> wordsInLineWithoutToken, int lineInd) {
        if (wordsInLineWithoutToken.size() != 3) {
            throw new ObjReaderException.ObjFormatException("The number of normal coordinates is incorrect", lineInd);
        }

        try {
            return new Vector3f(
                    Float.parseFloat(wordsInLineWithoutToken.get(0)),
                    Float.parseFloat(wordsInLineWithoutToken.get(1)),
                    Float.parseFloat(wordsInLineWithoutToken.get(2)));

        } catch(NumberFormatException e) {
            throw new ObjReaderException.ObjFormatException("Failed to parse float value.", lineInd);

        } catch(IndexOutOfBoundsException e) {
            throw new ObjReaderException.ObjFormatException("Too few normal arguments.", lineInd);
        }
    }

    protected static Polygon parseFace(final List<String> wordsInLineWithoutToken, int lineInd) {
        if (wordsInLineWithoutToken.size() < 3) {
            throw new ObjReaderException.ObjFormatException("Not enough information! A polygon requires at least three points!", lineInd);
        }

        List<Integer> onePolygonVertexIndices = new ArrayList<>();
        List<Integer> onePolygonTextureVertexIndices = new ArrayList<>();
        List<Integer> onePolygonNormalIndices = new ArrayList<>();

        for (String s : wordsInLineWithoutToken) {
            parseFaceWord(s, onePolygonVertexIndices, onePolygonTextureVertexIndices, onePolygonNormalIndices, lineInd);
        }

        //предотвращение f 1//3 1/2/3 1 1/2
        checkCorrectFaceFormat(onePolygonVertexIndices, onePolygonTextureVertexIndices, onePolygonNormalIndices, lineInd);

        Polygon result = new Polygon();
        result.setVertexIndices(onePolygonVertexIndices);
        result.setTextureVertexIndices(onePolygonTextureVertexIndices);
        result.setNormalIndices(onePolygonNormalIndices);
        return result;
    }

    protected static void parseFaceWord(
            String wordInLine,
            List<Integer> onePolygonVertexIndices,
            List<Integer> onePolygonTextureVertexIndices,
            List<Integer> onePolygonNormalIndices,
            int lineInd) {
        try {
            String[] wordIndices = wordInLine.split("/+");

            for (String value : wordIndices) {
                if (Integer.parseInt(value) < 0) {
                    throw new ObjReaderException.ObjContentException("Index cannot be negative!");
                }
            }
            switch (wordIndices.length) {
                case 1 -> onePolygonVertexIndices.add(Integer.parseInt(wordIndices[0]) - 1);
                case 2 -> {
                    onePolygonVertexIndices.add(Integer.parseInt(wordIndices[0]) - 1);
                    onePolygonTextureVertexIndices.add(Integer.parseInt(wordIndices[1]) - 1);
                }
                case 3 -> {
                    onePolygonVertexIndices.add(Integer.parseInt(wordIndices[0]) - 1);
                    onePolygonNormalIndices.add(Integer.parseInt(wordIndices[2]) - 1);
                    if (!wordIndices[1].equals("")) {
                        onePolygonTextureVertexIndices.add(Integer.parseInt(wordIndices[1]) - 1);
                    }
                }
                default -> throw new ObjReaderException.ObjFormatException("Invalid element size.", lineInd);
            }

        } catch(NumberFormatException e) {
            throw new ObjReaderException.ObjFormatException("Failed to parse int value.", lineInd);

        } catch(IndexOutOfBoundsException e) {
            throw new ObjReaderException.ObjFormatException("Too few arguments.", lineInd);
        }
    }
    protected static void checkCorrectFaceFormat(List<Integer> onePolygonVertexIndices,
                                                 List<Integer> onePolygonTextureVertexIndices,
                                                 List<Integer> onePolygonNormalIndices,
                                                 int lineInd) {
        if (onePolygonVertexIndices.size() != onePolygonTextureVertexIndices.size()
                && onePolygonVertexIndices.size()!=0 && onePolygonTextureVertexIndices.size()!=0) {
            throw new ObjReaderException.ObjFormatException("Incorrect format in the face description. Unreal situation!", lineInd);
        }
        if (onePolygonVertexIndices.size() != onePolygonNormalIndices.size()
                && onePolygonVertexIndices.size()!=0 && onePolygonNormalIndices.size() != 0) {
            throw new ObjReaderException.ObjFormatException("Incorrect format in the face description. Unreal situation!", lineInd);
        }
        if (onePolygonTextureVertexIndices.size() != onePolygonNormalIndices.size()
                && onePolygonTextureVertexIndices.size()!=0 && onePolygonNormalIndices.size() != 0) {
            throw new ObjReaderException.ObjFormatException("Incorrect format in the face description. Unreal situation!", lineInd);
        }
    }
}

