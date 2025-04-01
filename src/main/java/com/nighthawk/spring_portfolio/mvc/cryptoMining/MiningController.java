package com.nighthawk.spring_portfolio.mvc.cryptoMining;

import com.nighthawk.spring_portfolio.mvc.person.Person;
import com.nighthawk.spring_portfolio.mvc.person.PersonJpaRepository;
import com.nighthawk.spring_portfolio.mvc.userStocks.UserStocksRepository;
import com.nighthawk.spring_portfolio.mvc.userStocks.userStocksTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;
import java.text.SimpleDateFormat;

@RestController
@RequestMapping("/api/mining")
@Transactional
public class MiningController {
    @Autowired
    private PersonJpaRepository personRepository;
    
    @Autowired
    private MiningUserRepository miningUserRepository;
    
    @Autowired
    private GPURepository gpuRepository;

    @Autowired
    private MiningService miningService;

    private GPU getRandomBudgetGPU() {
        List<GPU> budgetGPUs = gpuRepository.findAll().stream()
            .filter(gpu -> gpu.getCategory().equals("Budget GPUs ($10000-20000)"))
            .collect(Collectors.toList());
        
        if (budgetGPUs.isEmpty()) {
            throw new RuntimeException("No budget GPUs found");
        }
        
        int randomIndex = (int) (Math.random() * budgetGPUs.size());
        return budgetGPUs.get(randomIndex);
    }

    private MiningUser getOrCreateMiningUser() {
        try {
            // Get authentication details
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                System.out.println("ERROR: Authentication object is null");
                throw new RuntimeException("No authentication context found");
            }

            String uid = auth.getName();
            System.out.println("\n=== Authentication Debug ===");
            System.out.println("UID: " + uid);
            System.out.println("Principal: " + auth.getPrincipal());
            System.out.println("Authorities: " + auth.getAuthorities());
            System.out.println("Is Authenticated: " + auth.isAuthenticated());

            if ("anonymousUser".equals(uid)) {
                System.out.println("WARNING: Anonymous user detected");
                throw new RuntimeException("User not authenticated");
            }

            // Find person by UID with detailed logging
            System.out.println("\n=== Person Lookup ===");
            System.out.println("Looking up person with UID: " + uid);
            Person person = personRepository.findByUid(uid);
            
            if (person == null) {
                System.out.println("ERROR: No person found for UID: " + uid);
                throw new RuntimeException("Person not found for UID: " + uid);
            }        

            System.out.println("Found person: " + person.getEmail());

            // Find or create mining user with detailed logging
            System.out.println("\n=== Mining User Lookup/Creation ===");
            return miningUserRepository.findByPerson(person)
                .map(existingUser -> {
                    System.out.println("Found existing mining user for: " + person.getEmail());
                    return existingUser;
                })
                .orElseGet(() -> {
                    System.out.println("Creating new mining user for: " + person.getEmail());
                    MiningUser newUser = new MiningUser(person);
                    
                    // Give new user a random budget GPU
                    GPU randomBudgetGPU = getRandomBudgetGPU();
                    newUser.addGPU(randomBudgetGPU);
                    System.out.println("Added random budget GPU: " + randomBudgetGPU.getName());
                    
                    MiningUser savedUser = miningUserRepository.save(newUser);
                    System.out.println("Successfully created new mining user with ID: " + savedUser.getId());
                    return savedUser;
                });

        } catch (Exception e) {
            System.out.println("\n=== ERROR in getOrCreateMiningUser ===");
            System.out.println("Error type: " + e.getClass().getName());
            System.out.println("Error message: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get or create mining user: " + e.getMessage(), e);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getMiningStats() {
        try {
            MiningUser user = getOrCreateMiningUser();
            Map<String, Object> stats = new HashMap<>();
            
            // Group GPUs by ID and count quantities
            Map<Long, Map<String, Object>> gpuGroups = new HashMap<>();
            for (GPU gpu : user.getOwnedGPUs()) {
                Long gpuId = gpu.getId();
                if (!gpuGroups.containsKey(gpuId)) {
                    Map<String, Object> gpuInfo = new HashMap<>();
                    gpuInfo.put("id", gpuId);
                    gpuInfo.put("name", gpu.getName());
                    gpuInfo.put("hashrate", gpu.getHashRate());
                    gpuInfo.put("power", gpu.getPowerConsumption());
                    gpuInfo.put("temp", gpu.getTemp());
                    gpuInfo.put("quantity", user.getGpuQuantity(gpuId));
                    gpuInfo.put("isActive", user.getActiveGPUs().contains(gpu));
                    gpuGroups.put(gpuId, gpuInfo);
                }
            }
            
            stats.put("gpus", new ArrayList<>(gpuGroups.values()));
            stats.put("btcBalance", String.format("%.8f", user.getBtcBalance()));
            stats.put("pendingBalance", String.format("%.8f", user.getPendingBalance()));
            stats.put("totalBalanceUSD", String.format("%.2f", (user.getPendingBalance() + user.getBtcBalance()) * MiningService.BTC_PRICE));
            stats.put("hashrate", String.format("%.2f", user.getCurrentHashrate()));
            stats.put("shares", user.getShares());
            stats.put("isMining", user.isMining());
            stats.put("currentPool", user.getCurrentPool());
            stats.put("powerConsumption", user.getPowerConsumption());
            stats.put("averageTemperature", user.getAverageTemperature());
            stats.put("dailyRevenue", user.getDailyRevenue());
            stats.put("powerCost", user.getPowerCost());
            stats.put("activeGPUsCount", user.getActiveGPUs().size());

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/shop")
    public ResponseEntity<?> getGPUShop() {
        try {
            MiningUser user = getOrCreateMiningUser();
            List<GPU> allGpus = gpuRepository.findAll();
            
            // Convert GPUs to maps with additional ownership info
            List<Map<String, Object>> gpuList = new ArrayList<>();
            for (GPU gpu : allGpus) {
                Map<String, Object> gpuInfo = new HashMap<>();
                gpuInfo.put("id", gpu.getId());
                gpuInfo.put("name", gpu.getName());
                gpuInfo.put("hashRate", gpu.getHashRate());
                gpuInfo.put("powerConsumption", gpu.getPowerConsumption());
                gpuInfo.put("temp", gpu.getTemp());
                gpuInfo.put("price", gpu.getPrice());
                gpuInfo.put("category", gpu.getCategory());
                gpuInfo.put("owned", user.ownsGPUById(gpu.getId()));
                gpuInfo.put("quantity", user.getGpuQuantity(gpu.getId()));
                
                gpuList.add(gpuInfo);
            }
            
            return ResponseEntity.ok(gpuList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/gpu/buy/{gpuId}")
    public ResponseEntity<?> buyGPU(@PathVariable Long gpuId, @RequestBody(required = false) Map<String, Integer> request) {
        try {
            System.out.println("\n=== Buy GPU Debug Log ===");
            System.out.println("GPU ID: " + gpuId);
            System.out.println("Request body: " + request);
            
            MiningUser user = getOrCreateMiningUser();
            System.out.println("User found: " + user.getPerson().getEmail());
            
            GPU gpu = gpuRepository.findById(gpuId)
                .orElseThrow(() -> new RuntimeException("GPU not found"));
            System.out.println("GPU found: " + gpu.getName());
            
            // Get quantity from request, default to 1 if not specified
            int quantity = (request != null && request.containsKey("quantity")) ? request.get("quantity") : 1;
            System.out.println("Quantity to purchase: " + quantity);
            
            // Get user's crypto balance
            Person person = user.getPerson();
            double currentBalance = person.getBalanceDouble();
            System.out.println("Current balance: $" + currentBalance);
            
            if (currentBalance < gpu.getPrice() * quantity) {
                System.out.println("Error: Insufficient balance");
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "Insufficient balance to buy these GPUs"
                    ));
            }

            // Deduct total GPU price from balance
            double newBalance = currentBalance - gpu.getPrice() * quantity;
            person.setBalanceString(newBalance);
            personRepository.save(person);
            System.out.println("New balance after purchase: $" + newBalance);
            
            // Add GPUs to user's inventory
            System.out.println("Adding " + quantity + " GPUs to inventory");
            for (int i = 0; i < quantity; i++) {
                user.addGPU(gpu);
            }
            
            // Save the updated user
            miningUserRepository.save(user);
            System.out.println("User saved successfully");
            
            // Print final GPU counts
            System.out.println("Final GPU counts:");
            System.out.println("Total GPUs: " + user.getOwnedGPUs().size());
            System.out.println("Active GPUs: " + user.getActiveGPUs().size());
            System.out.println("GPU quantities: " + user.getGpuQuantities());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", String.format("Successfully purchased %dx %s", quantity, gpu.getName()),
                "newBalance", String.format("%.2f", newBalance)
            ));
        } catch (Exception e) {
            System.out.println("\n=== Buy GPU Error ===");
            System.out.println("Error type: " + e.getClass().getName());
            System.out.println("Error message: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/toggle")
    public ResponseEntity<?> toggleMining() {
        try {
            MiningUser user = getOrCreateMiningUser();
            user.setMining(!user.isMining());
            miningUserRepository.save(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("isMining", user.isMining());
            response.put("message", user.isMining() ? "Mining started" : "Mining stopped");
            response.put("currentHashrate", user.getCurrentHashrate());
            response.put("activeGPUs", user.getActiveGPUs().size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/gpu/toggle/{gpuId}")
    public ResponseEntity<?> toggleGPU(@PathVariable Long gpuId) {
        try {
            MiningUser user = getOrCreateMiningUser();
            GPU gpu = gpuRepository.findById(gpuId)
                .orElseThrow(() -> new RuntimeException("GPU not found"));
    
            // Check if user owns this GPU
            if (!user.ownsGPUById(gpuId)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "You don't own this GPU"));
            }
    
            boolean isActive = user.toggleGPU(gpu);
            miningUserRepository.save(user);
    
            return ResponseEntity.ok(Map.of("success", true, "message", isActive ? "GPU activated" : "GPU deactivated", "isActive", isActive));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/state")
    public ResponseEntity<?> getMiningState() {
        try {
            System.out.println("\n=== Getting Mining State ===");
            
            MiningUser user = getOrCreateMiningUser();
            if (user == null) {
                System.out.println("ERROR: getOrCreateMiningUser returned null");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
            }

            System.out.println("Got mining user: " + user.getPerson().getEmail());
            
            // Calculate profitability
            try {
                miningService.calculateProfitability(user);
            } catch (Exception e) {
                System.out.println("WARNING: Error calculating profitability: " + e.getMessage());
            }

            // Create response
            Map<String, Object> stats = new HashMap<>();
            try {
                double pendingBTC = user.getPendingBalance();
                double confirmedBTC = user.getBtcBalance();
                
                // Add all stats...
                stats.put("btcBalance", String.format("%.8f", confirmedBTC));
                stats.put("btcBalanceUSD", String.format("%.2f", confirmedBTC * MiningService.BTC_PRICE));
                stats.put("pendingBalance", String.format("%.8f", pendingBTC));
                stats.put("pendingBalanceUSD", String.format("%.2f", pendingBTC * MiningService.BTC_PRICE));
                stats.put("totalBalanceUSD", String.format("%.2f", (pendingBTC + confirmedBTC) * MiningService.BTC_PRICE));
                
                // Mining status
                stats.put("hashrate", String.format("%.2f", user.getCurrentHashrate()));
                stats.put("shares", user.getShares());
                stats.put("isMining", user.isMining());
                stats.put("currentPool", user.getCurrentPool());
                
                // Power and temperature
                stats.put("powerConsumption", user.getPowerConsumption());
                stats.put("averageTemperature", user.getAverageTemperature());
                
                // Profitability
                stats.put("dailyRevenueBTC", String.format("%.8f", user.getDailyRevenue() / MiningService.BTC_PRICE));
                stats.put("dailyRevenueUSD", String.format("%.2f", user.getDailyRevenue()));
                stats.put("powerCost", user.getPowerCost());
                stats.put("netProfitUSD", String.format("%.2f", user.getDailyRevenue() - user.getPowerCost()));

                // GPU information - Group by ID
                Map<Long, Map<String, Object>> gpuGroups = new HashMap<>();
                for (GPU gpu : user.getOwnedGPUs()) {
                    Long gpuId = gpu.getId();
                    if (!gpuGroups.containsKey(gpuId)) {
                        Map<String, Object> gpuInfo = new HashMap<>();
                        gpuInfo.put("id", gpuId);
                        gpuInfo.put("name", gpu.getName());
                        gpuInfo.put("hashrate", gpu.getHashRate());
                        gpuInfo.put("power", gpu.getPowerConsumption());
                        gpuInfo.put("temp", gpu.getTemp());
                        gpuInfo.put("quantity", user.getGpuQuantity(gpuId));
                        gpuInfo.put("isActive", user.getActiveGPUs().contains(gpu));
                        gpuGroups.put(gpuId, gpuInfo);
                    }
                }
                
                stats.put("gpus", new ArrayList<>(gpuGroups.values()));

                // Group active GPUs by ID
                Map<Long, Map<String, Object>> activeGpuGroups = new HashMap<>();
                for (GPU gpu : user.getActiveGPUs()) {
                    Long gpuId = gpu.getId();
                    if (!activeGpuGroups.containsKey(gpuId)) {
                        Map<String, Object> gpuInfo = new HashMap<>();
                        gpuInfo.put("id", gpuId);
                        gpuInfo.put("name", gpu.getName());
                        gpuInfo.put("hashRate", gpu.getHashRate());
                        gpuInfo.put("powerConsumption", gpu.getPowerConsumption());
                        gpuInfo.put("temp", gpu.getTemp());
                        gpuInfo.put("price", gpu.getPrice());
                        gpuInfo.put("category", gpu.getCategory());
                        gpuInfo.put("available", gpu.isAvailable());
                        gpuInfo.put("efficiency", gpu.getEfficiency());
                        gpuInfo.put("quantity", user.getGpuQuantity(gpuId));
                        activeGpuGroups.put(gpuId, gpuInfo);
                    }
                }
                
                stats.put("activeGPUs", new ArrayList<>(activeGpuGroups.values()));

                System.out.println("Successfully compiled mining stats");
                return ResponseEntity.ok(stats);
                
            } catch (Exception e) {
                System.out.println("ERROR: Failed to compile mining stats: " + e.getMessage());
                throw e;
            }

        } catch (Exception e) {
            System.out.println("\n=== ERROR in getMiningState ===");
            System.out.println("Error type: " + e.getClass().getName());
            System.out.println("Error message: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get mining state");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("type", e.getClass().getName());
            errorResponse.put("timestamp", new Date().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    @PostMapping("/testMining")
    public ResponseEntity<?> testMining() {
        try {
            MiningUser user = getOrCreateMiningUser(); // Get or create the mining user
            user.setMining(true); // Ensure the user is set to mining

            // Simulate a hashrate for testing
            user.setCurrentHashrate(60.55); // Set hashrate to 60.55 MH/s

            // Call the mining process
            miningService.processMining(); // This should update the user's balance and shares

            // Prepare the response
            Map<String, Object> response = new HashMap<>();
            response.put("btcBalance", user.getBtcBalance());
            response.put("pendingBalance", user.getPendingBalance());
            response.put("shares", user.getShares());
            response.put("message", "Mining test completed successfully.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/mining-status")
    public ResponseEntity<?> getMiningStatus() {
        try {
            MiningUser user = getOrCreateMiningUser();
            
            // Get user's crypto balance
            Person person = user.getPerson();
            double currentBalance = person.getBalanceDouble();
            
            Map<String, Object> status = new HashMap<>();
            double pendingBTC = user.getPendingBalance();
            double confirmedBTC = user.getBtcBalance();
            
            // Calculate USD values using current balance
            double btcPrice = MiningService.BTC_PRICE;
            double pendingUSD = pendingBTC * btcPrice;
            double confirmedUSD = confirmedBTC * btcPrice;
            double totalUSD = pendingUSD + confirmedUSD;

            status.put("pendingBalance", String.format("%.8f", pendingBTC));
            status.put("pendingBalanceUSD", String.format("%.2f", pendingUSD));
            status.put("confirmedBalance", String.format("%.8f", confirmedBTC));
            status.put("confirmedBalanceUSD", String.format("%.2f", confirmedUSD));
            status.put("totalBalanceUSD", String.format("%.2f", totalUSD));
            status.put("userBalance", String.format("%.2f", currentBalance));
            status.put("isMining", user.isMining());
            status.put("activeGPUs", user.getActiveGPUs().size());
            status.put("currentHashrate", String.format("%.2f", user.getCurrentHashrate()));
            status.put("lastShare", user.getShares());
            status.put("estimated24hBTC", String.format("%.8f", user.getDailyRevenue() / MiningService.BTC_PRICE));
            status.put("estimated24hUSD", String.format("%.2f", user.getDailyRevenue()));
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/test-mining-system")
    public ResponseEntity<?> testMiningSystem() {
        try {
            MiningUser user = getOrCreateMiningUser();
            
            // Print initial state
            System.out.println("\n=== Mining System Test ===");
            System.out.println("Initial state:");
            System.out.println("- Mining active: " + user.isMining());
            System.out.println("- Active GPUs: " + user.getActiveGPUs().size());
            System.out.println("- GPU Details:");
            user.getActiveGPUs().forEach(gpu -> {
                System.out.println("  * " + gpu.getName() + " - " + gpu.getHashRate() + " MH/s");
            });
            System.out.println("- Current hashrate: " + user.getCurrentHashrate());
            System.out.println("- Initial balance: " + user.getBtcBalance());
            System.out.println("- Initial pending: " + user.getPendingBalance());
            
            // Force mining on and ensure GPUs are active
            user.setMining(true);
            if (user.getActiveGPUs().isEmpty() && !user.getOwnedGPUs().isEmpty()) {
                // Activate the first GPU if none are active
                user.toggleGPU(user.getOwnedGPUs().get(0));
            }
            miningUserRepository.save(user);
            
            // Manually trigger mining process
            miningService.processMining();
            
            // Wait for one mining cycle
            Thread.sleep(MiningService.MINING_INTERVAL);
            
            // Refresh user data
            user = getOrCreateMiningUser();
            
            // Print final state
            System.out.println("\nFinal state:");
            System.out.println("- Mining active: " + user.isMining());
            System.out.println("- Active GPUs: " + user.getActiveGPUs().size());
            System.out.println("- Current hashrate: " + user.getCurrentHashrate());
            System.out.println("- Final balance: " + user.getBtcBalance());
            System.out.println("- Final pending: " + user.getPendingBalance());
            System.out.println("=== Test Complete ===\n");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("initialBalance", user.getBtcBalance());
            response.put("finalPending", user.getPendingBalance());
            response.put("hashrate", user.getCurrentHashrate());
            response.put("activeGPUs", user.getActiveGPUs().size());
            response.put("ownedGPUs", user.getOwnedGPUs().size());
            response.put("miningEnabled", user.isMining());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Mining test failed: " + e.getMessage()));
        }
    }

    @GetMapping("/debug-values")
    public ResponseEntity<?> debugValues() {
        try {
            MiningUser user = getOrCreateMiningUser();
            Map<String, Object> debug = new HashMap<>();
            
            // Raw values
            debug.put("rawPendingBTC", user.getPendingBalance());
            debug.put("rawConfirmedBTC", user.getBtcBalance());
            debug.put("btcPrice", MiningService.BTC_PRICE);
            
            // Calculated values
            debug.put("pendingUSD", user.getPendingBalance() * MiningService.BTC_PRICE);
            debug.put("confirmedUSD", user.getBtcBalance() * MiningService.BTC_PRICE);
            debug.put("totalUSD", (user.getPendingBalance() + user.getBtcBalance()) * MiningService.BTC_PRICE);
            
            // Formatted values
            debug.put("formattedPendingUSD", String.format("%.2f", user.getPendingBalance() * MiningService.BTC_PRICE));
            debug.put("formattedConfirmedUSD", String.format("%.2f", user.getBtcBalance() * MiningService.BTC_PRICE));
            debug.put("formattedTotalUSD", String.format("%.2f", (user.getPendingBalance() + user.getBtcBalance()) * MiningService.BTC_PRICE));
            
            return ResponseEntity.ok(debug);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/gpu/sell/{gpuId}")
    public ResponseEntity<?> sellGPU(@PathVariable Long gpuId, @RequestBody(required = false) Map<String, Integer> request) {
        try {
            MiningUser user = getOrCreateMiningUser();
            GPU gpu = gpuRepository.findById(gpuId)
                .orElseThrow(() -> new RuntimeException("GPU not found"));

            // Get quantity to sell, default to 1
            int quantityToSell = (request != null && request.containsKey("quantity")) 
                ? request.get("quantity") 
                : 1;

            // Check if user owns enough GPUs
            int ownedQuantity = user.getGpuQuantity(gpuId);
            if (ownedQuantity < quantityToSell) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, 
                               "message", "You don't own enough GPUs"));
            }

            // Calculate sell price (80% of original price)
            double sellPrice = gpu.getPrice() * 0.8 * quantityToSell;

            // Update user's balance using Person
            Person person = user.getPerson();
            double currentBalance = person.getBalanceDouble();
            double newBalance = currentBalance + sellPrice;
            person.setBalanceString(newBalance);
            personRepository.save(person);

            // Remove GPUs from user's inventory
            user.removeGPUs(gpu, quantityToSell);

            // Save changes
            miningUserRepository.save(user);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", String.format("Successfully sold %dx %s for $%.2f", 
                                       quantityToSell, gpu.getName(), sellPrice),
                "newBalance", String.format("%.2f", newBalance)
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/gpu/sell/batch")
    public ResponseEntity<?> sellGPUsBatch(@RequestBody Map<String, List<Map<String, Object>>> request) {
        try {
            MiningUser user = getOrCreateMiningUser();
            List<Map<String, Object>> gpus = request.get("gpus");
            
            if (gpus == null || gpus.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, 
                               "message", "No GPUs selected"));
            }

            double totalSellPrice = 0.0;
            List<String> soldGPUs = new ArrayList<>();

            // Process each GPU sale
            for (Map<String, Object> gpuInfo : gpus) {
                Long gpuId = Long.parseLong(gpuInfo.get("id").toString());
                int quantity = Integer.parseInt(gpuInfo.get("quantity").toString());

                GPU gpu = gpuRepository.findById(gpuId)
                    .orElseThrow(() -> new RuntimeException("GPU not found: " + gpuId));

                if (user.getGpuQuantity(gpuId) < quantity) {
                    continue; // Skip if not enough quantity
                }

                double sellPrice = gpu.getPrice() * 0.8 * quantity;
                totalSellPrice += sellPrice;
                user.removeGPUs(gpu, quantity);
                soldGPUs.add(String.format("%dx %s", quantity, gpu.getName()));
            }

            // Update user's balance using Person
            Person person = user.getPerson();
            double currentBalance = person.getBalanceDouble();
            double newBalance = currentBalance + totalSellPrice;
            person.setBalanceString(newBalance);
            personRepository.save(person);

            // Save changes
            miningUserRepository.save(user);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", String.format("Successfully sold %s for $%.2f", 
                                       String.join(", ", soldGPUs), totalSellPrice),
                "newBalance", String.format("%.2f", newBalance)
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/test-new-user-gpu")
    public ResponseEntity<?> testNewUserGPU() {
        try {
            System.out.println("\n=== Testing New User GPU Assignment ===");
            
            // Get all budget GPUs first to verify they exist
            List<GPU> allBudgetGPUs = gpuRepository.findAll().stream()
                .filter(gpu -> gpu.getCategory().equals("Budget GPUs ($10000-20000)"))
                .collect(Collectors.toList());
            
            System.out.println("Found " + allBudgetGPUs.size() + " budget GPUs");
            if (allBudgetGPUs.isEmpty()) {
                throw new RuntimeException("No budget GPUs found in the database. Make sure DataInitializer has run.");
            }

            // Print all found budget GPUs
            System.out.println("\nAvailable Budget GPUs:");
            allBudgetGPUs.forEach(gpu -> {
                System.out.println(String.format("- %s (ID: %d, Price: $%.2f)", 
                    gpu.getName(), gpu.getId(), gpu.getPrice()));
            });

            // Get random GPU
            GPU randomBudgetGPU = allBudgetGPUs.get((int) (Math.random() * allBudgetGPUs.size()));
            System.out.println("\nRandomly selected GPU: " + randomBudgetGPU.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Test completed successfully");
            response.put("totalBudgetGPUs", allBudgetGPUs.size());
            response.put("selectedGPU", Map.of(
                "id", randomBudgetGPU.getId(),
                "name", randomBudgetGPU.getName(),
                "hashRate", randomBudgetGPU.getHashRate(),
                "price", randomBudgetGPU.getPrice(),
                "category", randomBudgetGPU.getCategory()
            ));
            response.put("allBudgetGPUs", allBudgetGPUs.stream()
                .map(gpu -> Map.of(
                    "id", gpu.getId(),
                    "name", gpu.getName(),
                    "hashRate", gpu.getHashRate(),
                    "price", gpu.getPrice(),
                    "category", gpu.getCategory()
                ))
                .collect(Collectors.toList())
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("\n=== Error in testNewUserGPU ===");
            System.out.println("Error type: " + e.getClass().getName());
            System.out.println("Error message: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", e.getMessage(),
                    "errorType", e.getClass().getSimpleName(),
                    "details", "Check server logs for more information"
                ));
        }
    }
}
