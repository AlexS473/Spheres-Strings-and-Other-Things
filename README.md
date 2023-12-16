# Spheres-Strings and Other Things

#### Build requirements 
- Java 11 (Language level 11)

#### Libraries 
- OpenGL/GLSL 
- JOGL (specifically these files: jogl-all, gluegen-rt)
- JOML (specifically this file: joml-1.10.5)

These are installed before building and running the program. 

### How to Use the Application 

The following Keys have functions:

1. Arrow Up - Rotate Camera Up 
2. Arrow Down - Rotate Camera Down 
3. Arrow Left - Rotate Camera Left 
4. Arrow Right - Rotate Camera Right 
5. Z - Zoom In 
6. X - Zoom Out 
7. A - Rotate Light Left
8. D - Rotate Light Right 
9. 1 - Raise first sphere 
10. 2 - Raise first 2 spheres 
11. 4 - Raise last 2 spheres 
12. 5 - Raise last sphere 
13. Space bar - Release spheres (start oscillation)

### Known Bugs 
- The depth of the objects sometimes get mixed up where the rotation is done slowly. For example, the spheres seem to overlap, the frames cross each other.
- The Shadows are mapped incorrectly, I think I didn't use the correct values for the look at matrix.
- The shadow also moves when the view is rotated. 
- (Kind of, but not really) If one of the keys to lift or release the spheres is pressed at the 'wrong' time there's a little lag before movement or extra movement. This is because I refuse to sync the time factor with the key events. I think it's fine as is. 
### Isn't a bug, is a feature 
- The 'iceburg' is a little mountainous, so it seems to cast its own shadows. 
