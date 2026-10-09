Here’s a 4-person split. About a third of the code already exists, so the new work divides fairly evenly.

What is already done
Your first 30%: Storable, Person, Actor, CrewMember, Director, Movie, ProductionTeam.
From my last message: SceneStatus, ResourceNotAvailableException, Bookable, Schedulable, Location, Equipment, Scene, ShootingSchedule. These are tested.
Still to write: Budget, BudgetExceededException, ProductionPhase, Timeline, ScheduleConflictException, Production, FileStorage, ConsoleMenu, and the final MovieProductionApp.
The split
Member	Owns	Work
1 (you)	Production, ScheduleConflictException, final MovieProductionApp	The central class and the integration. You already wrote the people side.
2	ProductionPhase, Timeline, plus checking and owning the resource and scene classes	Take the files from my last message, understand them and add them to the project. Then write Timeline, which uses Scene.getProgress(). This is the lightest load, so this person also does testing (see Member 4).
3	Budget, BudgetExceededException, FileStorage<T>, and CSV loading	Budget.addExpense throws when spending goes past the total. FileStorage saves and loads lists of Storable. Loading also needs a static fromCsv(String line) in the people, movie, location and equipment classes, and in Scene and ShootingSchedule.
4	ConsoleMenu	The whole text menu, with readInt and readDate that check input and re-ask when it is wrong.

What each member must finish

Member 1: Production

Fields from the diagram.
addScene, addSchedule, filterScenes(Predicate), sortSchedules(Comparator), advancePhase(), getOverallProgress(), saveAll() and loadAll().
addSchedule throws ScheduleConflictException when the same scene is scheduled twice, or two schedules overlap at the same time on the same date at the same location.
It calls schedule() from ShootingSchedule and records costs in the Budget.

Member 2: Timeline and the resource and scene classes

ProductionPhase is the enum PRE_PRODUCTION, PRODUCTION, POST_PRODUCTION.
Timeline has trackProgress(List<Scene>), which averages the scenes’ progress, and isOnSchedule(), which checks the current phase’s deadline against today.

Member 3: Budget, FileStorage, CSV

Budget.addExpense(category, amt) adds to the expenses map and to spentAmount. It throws BudgetExceededException if the total would be passed. getRemaining() returns total minus spent.
FileStorage<T extends Storable> has save(List<T>), load(Function<String,T>) and append(T), using FileWriter/BufferedReader (or Files.write / Files.readAllLines).
When loading a Scene or ShootingSchedule, look up the location or scene by id from the already-loaded lists. Agree the load order with Member 1: people, locations and equipment, then scenes, then schedules.

Member 4: ConsoleMenu

Menu options: add actor, add crew, add location, add equipment, create scene, schedule a scene, update scene progress, show budget, show timeline, advance phase, save, load, exit.
Use the Map<Integer, Runnable> from the diagram to look up the option, and wrap ResourceNotAvailableException and the other errors in try/catch.
Ground rules, so the code fits together
The diagram is the contract. Use the exact class names, method names and signatures. Everyone can write against them without waiting for the others, using empty stubs if needed.
Same folder, no packages, one owner per file. Don’t edit someone else’s file; ask them to change it.
Use Git, or one shared folder with a single person merging. Don’t email files around.
Compile everything together at the halfway mark. That is when most mismatches show up.
Missing in the diagram

Production has lists of actors, crew, locations and equipment but no methods to add to them, and the menu needs these. Add addActor, addCrew, addLocation, addEquipment, and getters for the teams, lists, Budget and Timeline. Also add the missing methods to Scene, as I listed in my last message. I can update the diagram once the code is final.

Timeline for tomorrow
Next 2 to 3 hours: everyone writes their own files against the diagram.
Then, an hour: put everything in one folder and fix compile errors together.
Then, 2 hours: Member 1 connects Production and main. Members 2 and 4 test every menu option, including wrong inputs and the clash cases (double booking, over-budget, same scene scheduled twice).
Last hour: a final run from a clean folder. Everyone reads all the code once, because any of you can be asked about any class in the viva.
