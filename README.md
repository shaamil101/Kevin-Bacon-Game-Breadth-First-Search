# Kevin-Bacon-Game-Breadth-First-Search
### This problem set tackles the important social network problem of finding an actor's "Bacon number". Starting with an actor, see if they have been in a movie with someone who has been in a movie with someone who has been in a movie ... who has been in a movie with Kevin Bacon. They're usually at most 6 steps away. There are plenty of other 6-degrees-of-separation phenomena in social networks. 

### In a geekier version, the center of the universe is Paul Erdos, a profilic author and coauthor, and people are characterized by their Erdos numbers. The highest known finite Erdos number is 13. Remarkably, there are a number of people who have both small Erdos numbers and small Bacon numbers (number = steps away). Dan Kleitman has total Erdos-Bacon number of 3 (Erdos 1, Bacon 2), but the Bacon number is due to a role as an extra. Danica McKellar has an Erdos-Bacon number of 6, and is both a professional actress (The Wonder Years and West Wing) and wrote a published math paper as well as supplemental math texts designed for teenage girls (Math Doesn't Suck, Kiss My Math, and Hot X: Algebra Exposed).

### In this problem set I will write code for social network analysis, and I will use it in variations on the Kevin Bacon game.

### In the Kevin Bacon game, the vertices are actors and the edge relationship is "appeared together in a movie". The goal is to find the shortest path between two actors. Traditionally the goal is to find the shortest path to Kevin Bacon, but we'll allow anybody to be the center of the acting universe. We'll also do some analyses to help find better Bacons. Since "degree" means the number of edges to/from a vertex, I'll refer to the number of steps away as the "separation" rather than the common "degrees of separation".



